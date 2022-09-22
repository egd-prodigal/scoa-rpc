package io.github.egd.prodigal.scoa.rpc.consumer;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.Applications;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Target;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.cloud.netflix.eureka.CloudEurekaClient;
import org.springframework.context.ApplicationListener;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ScoaRpcEurekaFeignHandler implements ApplicationListener<HeartbeatEvent>, RequestInterceptor, SmartInitializingSingleton {

    private final DiscoveryClient discoveryClient;

    public ScoaRpcEurekaFeignHandler(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    // type:version:group#method -> List<instance>
    private final Map<String, Set<String>> serviceMap = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private final ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();

    private final ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();


    @Override
    public void onApplicationEvent(HeartbeatEvent event) {
        Object source = event.getSource();
        if (source instanceof CloudEurekaClient) {
            CloudEurekaClient cloudEurekaClient = (CloudEurekaClient) source;
            Applications applications = cloudEurekaClient.getApplications();
            List<Application> registeredApplications = applications.getRegisteredApplications();
            writeLock.lock();
            serviceMap.clear();
            try {
                registeredApplications.forEach(application -> {
                    List<InstanceInfo> instances = application.getInstances();
                    instances.forEach(instance -> {
                        Map<String, String> metadata = instance.getMetadata();
                        if (metadata.containsKey("scoa.rpc.consumer")) {
                            String s = metadata.get("scoa.rpc.consumer");
                            refreshServiceMap(s, instance.getInstanceId());
                        }
                    });
                });
            } finally {
                writeLock.unlock();
            }
        }
    }

    @Override
    public void afterSingletonsInstantiated() {
        List<String> services = discoveryClient.getServices();
        writeLock.lock();
        try {
            services.forEach(service -> {
                List<ServiceInstance> instances = discoveryClient.getInstances(service);
                instances.forEach(instance -> {
                    Map<String, String> metadata = instance.getMetadata();
                    if (metadata.containsKey("scoa.rpc.consumer")) {
                        String s = metadata.get("scoa.rpc.consumer");
                        refreshServiceMap(s, instance.getInstanceId());
                    }
                });
            });
        } finally {
            writeLock.unlock();
        }
    }

    private void refreshServiceMap(String s, String instanceId) {
        String[] consumerArray = s.split(",");
        for (String consumer : consumerArray) {
            int i = consumer.indexOf("[");
            int j = consumer.indexOf("]");
            String service = consumer.substring(0, i);
            String methods = consumer.substring(i + 1, j);
            String[] methodArray = methods.split("&");
            for (String method : methodArray) {
                String key = service + "#" + method;
                if (!serviceMap.containsKey(key)) {
                    serviceMap.put(key, new CopyOnWriteArraySet<>());
                }
                serviceMap.get(key).add(instanceId);
            }
        }
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        Target<?> target = requestTemplate.feignTarget();
        Map<String, Collection<String>> headers = requestTemplate.headers();
        Collection<String> collection = headers.get("scoa-rpc-attach");
        if (collection == null || collection.isEmpty()) {
            return;
        }
        String attach = new ArrayList<>(collection).get(0);
        Class<?> type = target.type();
        readLock.lock();
        try {
            String key = type.getName() + ":" + attach;
            Set<String> strings = serviceMap.get(key);
            System.out.println(strings);
        } finally {
            readLock.unlock();
        }
    }

}
