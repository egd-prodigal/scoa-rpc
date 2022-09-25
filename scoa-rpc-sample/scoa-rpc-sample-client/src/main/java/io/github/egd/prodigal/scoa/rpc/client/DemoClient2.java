package io.github.egd.prodigal.scoa.rpc.client;

import io.github.egd.prodigal.scoa.rpc.dto.User;

public interface DemoClient2 {

    String hello();

    User getUser();

    User getUserByUsername(String username);

    void saveUser(User user);

}
