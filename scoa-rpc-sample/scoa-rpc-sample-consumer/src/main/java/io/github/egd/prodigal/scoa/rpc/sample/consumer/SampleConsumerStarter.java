package io.github.egd.prodigal.scoa.rpc.sample.consumer;

import io.github.egd.prodigal.scoa.rpc.consumer.EnableScoaRpcConsumer;
import io.github.egd.prodigal.scoa.rpc.consumer.ScoaRpcInvocationInterceptor;
import io.github.egd.prodigal.scoa.rpc.consumer.ScoaRpcInvocationInterceptorChain;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@SpringBootApplication
@EnableEurekaClient
@EnableDiscoveryClient
@EnableFeignClients
@EnableScoaRpcConsumer(basePackages = "io.github.egd.prodigal.scoa.rpc.client")
public class SampleConsumerStarter {

    public static void main(String[] args) {
        SpringApplication.run(SampleConsumerStarter.class, args);
    }

    @Bean
    public ScoaRpcInvocationInterceptor scoaRpcInvocationInterceptor() {
        return httpHolder -> {
            URI uri = httpHolder.getRequest().getURI();
            System.out.println(uri);
        };
    }

}
