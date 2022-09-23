package io.github.egd.prodigal.scoa.rpc.sample.consumer.web;

import io.github.egd.prodigal.scoa.rpc.annotations.ScoaRpcConsumer;
import io.github.egd.prodigal.scoa.rpc.sample.consumer.integration.DemoApi;
import io.github.egd.prodigal.scoa.rpc.sample.consumer.integration.DemoClientIntegration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
public class DemoController {


    @ScoaRpcConsumer
    private RestTemplate restTemplate;

    @Autowired
    private DemoApi demoApi;

    @Autowired
    DemoClientIntegration demoClientIntegration;

    @RequestMapping("/demo")
    public String demo() {
        Map<String, Object> demo = demoApi.demo();
        System.out.println(demo);
        Object test = demoClientIntegration.test();
        System.out.println(test);
        ResponseEntity<String> response = restTemplate.getForEntity("http://sample-provider/demo", String.class);
        return response.getBody();
    }


}
