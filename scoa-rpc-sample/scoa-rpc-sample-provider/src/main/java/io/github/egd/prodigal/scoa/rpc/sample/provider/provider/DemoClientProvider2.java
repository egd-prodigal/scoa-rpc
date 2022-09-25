package io.github.egd.prodigal.scoa.rpc.sample.provider.provider;

import io.github.egd.prodigal.scoa.rpc.annotations.ScoaRpcProvider;
import io.github.egd.prodigal.scoa.rpc.client.DemoClient2;
import io.github.egd.prodigal.scoa.rpc.dto.User;
import io.github.egd.prodigal.scoa.rpc.sample.provider.service.DemoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@ScoaRpcProvider(version = "1.0.1", group = "sample")
public class DemoClientProvider2 implements DemoClient2 {

    private final Logger logger = LoggerFactory.getLogger(DemoClientProvider2.class);

    @Autowired
    private DemoService demoService;

    @Override
    public String hello() {
        return "hello, world";
    }

    @Override
    public User getUser() {
        logger.info("getUser");
        return demoService.getUser();
    }

    @Override
    public User getUserByUsername(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail("yeeminshon@outlook.com");
        return user;
    }

    @Override
    public void saveUser(User user) {
        logger.info("save user, {}", user);
    }


}
