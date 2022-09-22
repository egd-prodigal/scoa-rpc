package io.github.egd.prodigal.scoa.rpc.sample.consumer.integration;

import io.github.egd.prodigal.scoa.rpc.annotations.ScoaRpcConsumer;
import io.github.egd.prodigal.scoa.rpc.client.DemoClient;
import org.springframework.stereotype.Service;

@Service
public class DemoClientIntegration {

    @ScoaRpcConsumer(version = "1.0.1", group = "sample")
    private DemoClient demoClient;

}
