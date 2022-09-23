package io.github.egd.prodigal.scoa.rpc.consumer;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.Applications;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.cloud.netflix.eureka.CloudEurekaClient;
import org.springframework.context.ApplicationListener;

import java.util.List;
import java.util.Map;


public class ScoaRpcConsumerContext implements ApplicationListener<HeartbeatEvent> {

    @Override
    public void onApplicationEvent(HeartbeatEvent event) {
        Object source = event.getSource();
        System.out.println(source);
        CloudEurekaClient cloudEurekaClient = (CloudEurekaClient) source;
        Applications applications = cloudEurekaClient.getApplications();
        List<Application> registeredApplications = applications.getRegisteredApplications();
        registeredApplications.forEach(registeredApplication -> {
            List<InstanceInfo> instances = registeredApplication.getInstances();
            instances.forEach(instanceInfo -> {
                Map<String, String> metadata = instanceInfo.getMetadata();
                if (metadata.containsKey("scoa.rpc.provider")) {
                    String rpcProvider = metadata.get("scoa.rpc.provider");
                    System.out.println(registeredApplication.getName() + " ---> " + rpcProvider);
                }
            });
        });
    }

}
