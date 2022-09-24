package io.github.egd.prodigal.scoa.rpc.sample.consumer.web;

import io.github.egd.prodigal.scoa.rpc.annotations.ScoaRpcConsumer;
import io.github.egd.prodigal.scoa.rpc.client.DemoClient;
import io.github.egd.prodigal.scoa.rpc.dto.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    private final Logger logger = LoggerFactory.getLogger(DemoController.class);

    @ScoaRpcConsumer(version = "1.0.1", group = "sample")
    private DemoClient demoClient;

    @RequestMapping("/demo")
    public User demo() {
        User user = demoClient.getUser();
        user = demoClient.getUserByUsername("yeeminshon");
        demoClient.saveUser(user);
        return user;
    }


}
