package io.github.egd.prodigal.symsa.rpc.sample.producer.web;

import feign.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@RestController
public class DemoController {


    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping("/demo")
    public String demo() {
        Feign feign = Feign.builder().requestInterceptor(new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {

            }
        }).client(new Client() {
            @Override
            public Response execute(Request request, Request.Options options) throws IOException {
                return null;
            }
        }).build();
        ResponseEntity<String> response = restTemplate.getForEntity("http://sample-consumer/demo", String.class);
        return response.getBody();
    }

}
