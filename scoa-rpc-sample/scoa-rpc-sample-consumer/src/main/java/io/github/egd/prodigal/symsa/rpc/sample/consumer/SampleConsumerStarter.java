package io.github.egd.prodigal.symsa.rpc.sample.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class SampleConsumerStarter {

    public static void main(String[] args) {
        SpringApplication.run(SampleConsumerStarter.class, args);
    }

}
