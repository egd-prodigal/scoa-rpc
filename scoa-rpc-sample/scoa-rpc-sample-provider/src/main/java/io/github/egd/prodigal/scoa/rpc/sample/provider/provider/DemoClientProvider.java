package io.github.egd.prodigal.scoa.rpc.sample.provider.provider;

import io.github.egd.prodigal.scoa.rpc.annotations.ScoaRpcProvider;
import io.github.egd.prodigal.scoa.rpc.client.DemoClient;
import io.github.egd.prodigal.scoa.rpc.dto.User;

@ScoaRpcProvider(version = "1.0.1", group = "sample")
public class DemoClientProvider implements DemoClient {

    @Override
    public String hello() {
        return "hello, world";
    }

    @Override
    public User getUser() {
        User user = new User();
        user.setUsername("yeemin");
        user.setEmail("yeeminshon@hotmail.com");
        return user;
    }

    @Override
    public User getUserByUsername(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail("yeeminshon@hotmail.com");
        return user;
    }


}
