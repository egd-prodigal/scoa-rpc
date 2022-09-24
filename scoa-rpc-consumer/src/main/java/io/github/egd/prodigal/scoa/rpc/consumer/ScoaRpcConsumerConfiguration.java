package io.github.egd.prodigal.scoa.rpc.consumer;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ScoaRpcConsumerConfiguration {

    @Bean
    public ScoaRpcConsumerBeanProcessor scoaRpcConsumerBeanProcessor() {
        return new ScoaRpcConsumerBeanProcessor();
    }

    @Bean("scoaRpcConsumerRestTemplate")
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ScoaRpcConsumerServiceHolder scoaRpcConsumerContext() {
        return new ScoaRpcConsumerServiceHolder();
    }

}
