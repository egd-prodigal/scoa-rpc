package io.github.egd.prodigal.scoa.rpc.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
public class ScoaRpcConsumerConfiguration {

    @Bean
    public ScoaRpcConsumerBeanProcessor scoaRpcConsumerBeanProcessor() {
        return new ScoaRpcConsumerBeanProcessor();
    }


    @Bean("scoaRpcConsumerRestTemplate")
    public RestTemplate restTemplate(@Autowired List<ScoaRpcInvocationInterceptor> invocationInterceptors) {
        RestTemplate restTemplate = new RestTemplate();
        List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
        interceptors.add(new ScoaRpcInvocationInterceptorChain(invocationInterceptors));
        restTemplate.setInterceptors(interceptors);
        return restTemplate;
    }

    @Bean
    public ScoaRpcConsumerServiceHolder scoaRpcConsumerContext() {
        return new ScoaRpcConsumerServiceHolder();
    }

}
