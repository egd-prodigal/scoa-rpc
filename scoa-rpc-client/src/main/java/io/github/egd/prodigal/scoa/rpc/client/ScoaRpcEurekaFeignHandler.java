package io.github.egd.prodigal.scoa.rpc.client;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoaRpcEurekaFeignHandler implements ApplicationListener<HeartbeatEvent>, RequestInterceptor, SmartInitializingSingleton {

    private final DiscoveryClient discoveryClient;

    public ScoaRpcEurekaFeignHandler(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    // serviceId, methodId, version, List<instance>
    private Map<String, Map<String, Map<String, List<String>>>> map = new HashMap<>();

    @Override
    public void onApplicationEvent(HeartbeatEvent event) {
        Object source = event.getSource();
        if (source instanceof CloudEurekaClient) {
            CloudEurekaClient cloudEurekaClient = (CloudEurekaClient) source;
            Applications applications = cloudEurekaClient.getApplications();
            List<Application> registeredApplications = applications.getRegisteredApplications();
            registeredApplications.forEach(application -> {
                System.out.println(application.toString());
                String name = application.getName();
                System.out.println(name);
                List<InstanceInfo> instances = application.getInstances();
                instances.forEach(instance -> {
                    System.out.println(instance.toString());
                    Map<String, String> metadata = instance.getMetadata();
                });
            });
        }
    }

    @Override
    public void afterSingletonsInstantiated() {
        List<String> services = discoveryClient.getServices();
        System.out.println(services);
        services.forEach(service -> {
            List<ServiceInstance> instances = discoveryClient.getInstances(service);
            instances.forEach(instance -> {
                String serviceId = instance.getServiceId();
                System.out.println(serviceId);
                Map<String, String> metadata = instance.getMetadata();
                if (metadata.containsKey("scoa.rpc.consumer")) {
                    String s = metadata.get("scoa.rpc.consumer");
                }
            });
        });
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        Target<?> target = requestTemplate.feignTarget();

//        requestTemplate.target("");
    }

}
