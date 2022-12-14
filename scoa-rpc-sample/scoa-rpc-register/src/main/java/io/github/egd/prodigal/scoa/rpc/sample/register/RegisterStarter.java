package io.github.egd.prodigal.scoa.rpc.sample.register;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class RegisterStarter {

    public static void main(String[] args) {
        SpringApplication.run(RegisterStarter.class, args);
    }

}
