package io.github.egd.prodigal.scoa.rpc.sample.provider;

import io.github.egd.prodigal.scoa.rpc.provider.EnableScoaRpcProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableEurekaClient
@EnableDiscoveryClient
@EnableScoaRpcProvider(basePackages = "io.github.egd.prodigal.scoa.rpc.sample.provider.provider")
public class SampleProviderStarter {

    public static void main(String[] args) {
        SpringApplication.run(SampleProviderStarter.class, args);
    }

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
