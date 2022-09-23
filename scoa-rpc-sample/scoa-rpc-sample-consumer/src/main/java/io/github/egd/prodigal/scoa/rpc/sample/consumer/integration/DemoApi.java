package io.github.egd.prodigal.scoa.rpc.sample.consumer.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@FeignClient(value = "sample-provider")
public interface DemoApi {

    @RequestMapping("/demo")
    Map<String, Object> demo();


}
