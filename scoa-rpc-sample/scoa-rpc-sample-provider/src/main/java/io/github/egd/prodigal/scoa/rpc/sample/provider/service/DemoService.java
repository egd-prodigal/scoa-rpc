package io.github.egd.prodigal.scoa.rpc.sample.provider.service;

import io.github.egd.prodigal.scoa.rpc.dto.User;
import org.springframework.stereotype.Service;

@Service
public class DemoService {

    public User getUser() {
        User user = new User();
        user.setUsername("yeemin");
        user.setEmail("yeeminshon@hotmail.com");
        return user;
    }

}
