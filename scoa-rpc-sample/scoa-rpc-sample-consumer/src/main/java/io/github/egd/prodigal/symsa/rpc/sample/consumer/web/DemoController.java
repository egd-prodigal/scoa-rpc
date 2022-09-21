package io.github.egd.prodigal.symsa.rpc.sample.consumer.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
public class DemoController {


    @RequestMapping("/demo")
    public Map<String, Object> demo() {
        return Collections.singletonMap("greetings", "hello");
    }

}
