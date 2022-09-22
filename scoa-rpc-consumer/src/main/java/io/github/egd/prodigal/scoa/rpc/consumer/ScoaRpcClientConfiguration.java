package io.github.egd.prodigal.scoa.rpc.consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScoaRpcClientConfiguration {

    @Bean
    public ScoaRpcConsumerBeanProcessor scoaRpcConsumerBeanProcessor() {
        return new ScoaRpcConsumerBeanProcessor();
    }


}
