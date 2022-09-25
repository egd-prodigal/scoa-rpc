package io.github.egd.prodigal.scoa.rpc.sample.consumer;

import io.github.egd.prodigal.scoa.rpc.consumer.EnableScoaRpcConsumer;
import io.github.egd.prodigal.scoa.rpc.consumer.ScoaRpcInvocationHttpHolder;
import io.github.egd.prodigal.scoa.rpc.consumer.ScoaRpcInvocationInterceptor;
import io.github.egd.prodigal.scoa.rpc.dto.User;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.http.client.ClientHttpResponse;

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
    @Order(1)
    public ScoaRpcInvocationInterceptor scoaRpcInvocationInterceptor() {
        return new ScoaRpcInvocationInterceptor() {
            @Override
            public void preInterceptor(ScoaRpcInvocationHttpHolder httpHolder) {
                URI uri = httpHolder.getRequest().getURI();
                User object = new User();
                object.setUsername("aaa");
                httpHolder.addContext("test", object);
                System.out.println(uri);
            }
        };
    }

    @Bean
    @Order(2)
    public ScoaRpcInvocationInterceptor scoaRpcInvocationInterceptor2() {
        return new ScoaRpcInvocationInterceptor() {
            @Override
            public ClientHttpResponse afterInterceptor(ScoaRpcInvocationHttpHolder httpHolder, ClientHttpResponse response) {
                System.out.println(httpHolder.getContext("test"));
                return ScoaRpcInvocationInterceptor.super.afterInterceptor(httpHolder, response);
            }
        };
    }

}
