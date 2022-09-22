package io.github.egd.prodigal.scoa.rpc.sample.provider.provider;

import io.github.egd.prodigal.scoa.rpc.annotations.ScoaRpcProvider;
import io.github.egd.prodigal.scoa.rpc.client.DemoClient;

@ScoaRpcProvider(version = "1.0.1", group = "sample")
public class DemoClientProvider implements DemoClient {

    @Override
    public String hello() {
        return "hello, world";
    }


}
