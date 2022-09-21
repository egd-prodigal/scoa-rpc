package io.github.egd.prodigal.scoa.rpc.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClientConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureBefore(EurekaDiscoveryClientConfiguration.class)
public class ScoaRpcClientConfiguration {

    @Bean
    public ScoaRpcEurekaFeignHandler scoaRpcEurekaCacheRefreshListener(@Autowired DiscoveryClient discoveryClient) {
        return new ScoaRpcEurekaFeignHandler(discoveryClient);
    }

}
