package io.github.egd.prodigal.scoa.rpc.consumer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.Applications;
import com.netflix.loadbalancer.DynamicServerListLoadBalancer;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.cloud.netflix.eureka.CloudEurekaClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;


public class ScoaRpcConsumerServiceHolder implements ApplicationListener<HeartbeatEvent>, ApplicationContextAware, SmartInitializingSingleton {

    private final Logger logger = LoggerFactory.getLogger(ScoaRpcConsumerServiceHolder.class);

    private static final String SCOA_RPC_PROVIDER = "scoa.rpc.provider";

    private static final JsonObject providerHolder = new JsonObject();

    private static final Map<String, ILoadBalancer> loadBalancerMap = new HashMap<>();

    private ApplicationContext applicationContext;

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private final ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();

    private final ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();

    @Override
    public void onApplicationEvent(HeartbeatEvent event) {
        Object source = event.getSource();
        CloudEurekaClient cloudEurekaClient = (CloudEurekaClient) source;
        Applications applications = cloudEurekaClient.getApplications();
        List<Application> registeredApplications = applications.getRegisteredApplications();
        registeredApplications.forEach(registeredApplication -> {
            List<InstanceInfo> instances = registeredApplication.getInstances();
            instances.forEach(instanceInfo -> {
                Map<String, String> metadata = instanceInfo.getMetadata();
                if (metadata.containsKey(SCOA_RPC_PROVIDER)) {
                    String rpcProvider = metadata.get(SCOA_RPC_PROVIDER);
                    registerRpcProvider(registeredApplication.getName(), rpcProvider);
                }
            });
        });
    }

    @Override
    public void afterSingletonsInstantiated() {
        DiscoveryClient discoveryClient = applicationContext.getBean(DiscoveryClient.class);
        List<String> services = discoveryClient.getServices();
        services.forEach(service -> {
            List<ServiceInstance> instances = discoveryClient.getInstances(service);
            instances.forEach(serviceInstance -> {
                Map<String, String> metadata = serviceInstance.getMetadata();
                if (metadata.containsKey(SCOA_RPC_PROVIDER)) {
                    String rpcProvider = metadata.get(SCOA_RPC_PROVIDER);
                    registerRpcProvider(service, rpcProvider);
                }
            });
        });
    }

    private synchronized void registerRpcProvider(String serviceId, String rpcProvider) {
        logger.info("serviceId: {}, rpc-provider: {}", serviceId, rpcProvider);
        JsonObject providers = new JsonObject();
        String[] rpcProviderArray = rpcProvider.split("&");
        for (String rpcProviderPackage : rpcProviderArray) {
            int leftBraceIndex = rpcProviderPackage.indexOf("{");
            int rightBraceIndex = rpcProviderPackage.indexOf("}");
            String packageName = rpcProviderPackage.substring(0, leftBraceIndex);
            JsonObject packageJson = providers.getAsJsonObject(packageName);
            if (packageJson == null) {
                packageJson = new JsonObject();
                providers.add(packageName, packageJson);
            }
            String classInfos = rpcProviderPackage.substring(leftBraceIndex + 1, rightBraceIndex);
            String[] classArray = classInfos.split(";");
            for (String classInfo : classArray) {
                int leftBracketIndex = classInfo.indexOf("[");
                int rightBracketIndex = classInfo.indexOf("]");
                String classVersionGroupInfo = classInfo.substring(0, leftBracketIndex);
                String[] classVersionGroupArray = classVersionGroupInfo.split(":");
                String className = classVersionGroupArray[0];
                JsonObject classJson = packageJson.getAsJsonObject(className);
                if (classJson == null) {
                    classJson = new JsonObject();
                    packageJson.add(className, classJson);
                }
                String version = classVersionGroupArray[1];
                JsonObject versionJson = classJson.getAsJsonObject(version);
                if (versionJson == null) {
                    versionJson = new JsonObject();
                    classJson.add(version, versionJson);
                }
                String group = classVersionGroupArray[2];
                JsonObject groupJson = versionJson.getAsJsonObject(group);
                if (groupJson == null) {
                    groupJson = new JsonObject();
                    versionJson.add(group, groupJson);
                }
                String methodInfos = classInfos.substring(leftBracketIndex + 1, rightBracketIndex);
                String[] methodArray = methodInfos.split("#");
                for (String methodInfo : methodArray) {
                    String methodName, parameterNames;
                    int leftParenthesisIndex = methodInfo.indexOf("(");
                    int rightParenthesisIndex = methodInfo.indexOf(")");
                    if (leftParenthesisIndex > -1 && rightParenthesisIndex > -1) {
                        methodName = methodInfo.substring(0, leftParenthesisIndex);
                        parameterNames = methodInfo.substring(leftParenthesisIndex + 1, rightParenthesisIndex);
                    } else {
                        parameterNames = "()";
                        methodName = methodInfo;
                    }
                    JsonObject methodJson = groupJson.getAsJsonObject(methodName);
                    if (methodJson == null) {
                        methodJson = new JsonObject();
                        groupJson.add(methodName, methodJson);
                    }
                    JsonArray parameterJson = methodJson.getAsJsonArray(parameterNames);
                    if (parameterJson == null) {
                        parameterJson = new JsonArray();
                        methodJson.add(parameterNames, parameterJson);
                    }
                    parameterJson.add(serviceId);
                }
            }
        }
        writeLock.lock();
        try {
            providerHolder.remove("providers");
            providerHolder.add("providers", providers);
            loadBalancerMap.clear();
        } finally {
            writeLock.unlock();
        }
    }

    public String chooseServiceId(String packageName, String className, String version, String group,
                                  String methodName, String... parameterTypes) {
        String key = String.join(",", packageName, className, version, group, methodName,
                parameterTypes == null ? "" : String.join(",", parameterTypes));
        ILoadBalancer loadBalancer;
        readLock.lock();
        try {
            loadBalancer = loadBalancerMap.get(key);
        } finally {
            readLock.unlock();
        }
        if (loadBalancer == null) {
            List<String> services = chooseServices(packageName, className, version, group, methodName, parameterTypes);
            loadBalancer = new DynamicServerListLoadBalancer<>();
            loadBalancer.addServers(services.stream().map(Server::new).collect(Collectors.toList()));
            writeLock.lock();
            try {
                loadBalancerMap.put(key, loadBalancer);
            } finally {
                writeLock.unlock();
            }
        }
        Server server = loadBalancer.chooseServer(null);
        return server.getHost();
    }

    public List<String> chooseServices(String packageName, String className, String version, String group,
                                       String methodName, String... parameterTypes) {
        JsonObject providers;
        readLock.lock();
        try {
            providers = providerHolder.getAsJsonObject("providers");
        } finally {
            readLock.unlock();
        }
        if (providers == null) {
            String message = "no provider find, please wait for provider refresh";
            logger.error(message);
            throw new RuntimeException(message);
        }
        JsonObject packageElement = providers.getAsJsonObject(packageName);
        if (packageElement != null) {
            JsonObject classElement = packageElement.getAsJsonObject(className);
            if (classElement != null) {
                JsonObject versionElement = classElement.getAsJsonObject(version);
                if (versionElement != null) {
                    JsonObject groupElement = versionElement.getAsJsonObject(group);
                    if (groupElement != null) {
                        JsonObject methodElement = groupElement.getAsJsonObject(methodName);
                        if (methodElement != null) {
                            String parameterNames;
                            if (parameterTypes == null) {
                                parameterNames = "()";
                            } else {
                                parameterNames = String.join(",", parameterTypes);
                            }
                            JsonArray serviceIdArray = methodElement.getAsJsonArray(parameterNames);
                            if (serviceIdArray != null && !serviceIdArray.isEmpty()) {
                                List<String> serviceIds = new ArrayList<>(serviceIdArray.size());
                                for (JsonElement serviceId : serviceIdArray) {
                                    serviceIds.add(serviceId.getAsString());
                                }
                                return serviceIds;
                            }
                        }
                    }
                }
            }
        }
        String message = String.format("cannot find service by provider: %s.%s:%s:%s#%s(%s)", packageName, className,
                version, group, methodName, parameterTypes == null ? "" : String.join(",", parameterTypes));
        logger.error(message);
        throw new RuntimeException(message);
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
