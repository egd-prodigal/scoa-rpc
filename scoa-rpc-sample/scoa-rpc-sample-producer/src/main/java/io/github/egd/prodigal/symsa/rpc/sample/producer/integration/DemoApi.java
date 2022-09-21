package io.github.egd.prodigal.symsa.rpc.sample.producer.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@FeignClient(value = "sample-consumer")
public interface DemoApi {

    @RequestMapping("/demo")
    Map<String, Object> demo();


}
