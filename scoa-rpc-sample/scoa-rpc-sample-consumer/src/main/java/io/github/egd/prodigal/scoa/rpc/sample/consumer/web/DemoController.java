package io.github.egd.prodigal.scoa.rpc.sample.consumer.web;

import io.github.egd.prodigal.scoa.rpc.sample.consumer.integration.DemoApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
public class DemoController {


    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DemoApi demoApi;

    @RequestMapping("/demo")
    public String demo() {
        Map<String, Object> demo = demoApi.demo();
        System.out.println(demo);
        ResponseEntity<String> response = restTemplate.getForEntity("http://sample-consumer/demo", String.class);
        return response.getBody();
    }


}
