package io.github.egd.prodigal.scoa.rpc.sample.consumer.web;

import io.github.egd.prodigal.scoa.rpc.annotations.ScoaRpcConsumer;
import io.github.egd.prodigal.scoa.rpc.client.DemoClient;
import io.github.egd.prodigal.scoa.rpc.client.DemoClient2;
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

    @ScoaRpcConsumer(version = "2.0.2", group = "test")
    private DemoClient2 demoClient2;

    @RequestMapping("/demo")
    public User demo() {
        User user = demoClient.getUser();
        user = demoClient.getUserByUsername("yeeminshon");
        demoClient2.saveUser(user);
        return user;
    }


}
