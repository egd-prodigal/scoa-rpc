package io.github.egd.prodigal.scoa.rpc.provider;

import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@AutoConfigureBefore()
public class ScoaRpcProviderConfiguration {

    @Bean
    public ScoaRpcProviderBeanProcessor scoaRpcProviderBeanProcessor(@Autowired Environment environment) {
        String port = environment.getProperty("scoa.rpc.provider.port", RandomUtils.nextInt(20000, 30000) + "");
        return new ScoaRpcProviderBeanProcessor(Integer.parseInt(port));
    }

    @Bean
    public ScoaRpcProviderServer scoaRpcProviderServer(@Autowired ScoaRpcProviderBeanProcessor scoaRpcProviderBeanProcessor) {
        return new ScoaRpcProviderServer(scoaRpcProviderBeanProcessor.getPort());
    }

}
