package io.github.egd.prodigal.scoa.rpc.sample.provider.provider;

import io.github.egd.prodigal.scoa.rpc.annotations.ScoaRpcProvider;
import io.github.egd.prodigal.scoa.rpc.client.DemoClient;
import io.github.egd.prodigal.scoa.rpc.dto.User;
import io.github.egd.prodigal.scoa.rpc.sample.provider.service.DemoService;
import org.springframework.beans.factory.annotation.Autowired;

@ScoaRpcProvider(version = "1.0.1", group = "sample")
public class DemoClientProvider implements DemoClient {

    @Autowired
    private DemoService demoService;

    @Override
    public String hello() {
        return "hello, world";
    }

    @Override
    public User getUser() {
        return demoService.getUser();
    }

    @Override
    public User getUserByUsername(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail("yeeminshon@outlook.com");
        return user;
    }


}
