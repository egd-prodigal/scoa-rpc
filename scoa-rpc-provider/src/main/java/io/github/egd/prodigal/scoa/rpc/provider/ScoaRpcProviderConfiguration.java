package io.github.egd.prodigal.scoa.rpc.provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@EnableConfigurationProperties(ScoaRpcProviderConfigBean.class)
public class ScoaRpcProviderConfiguration {

    @Bean
    public ScoaRpcProviderBeanProcessor scoaRpcProviderBeanProcessor(@Autowired ScoaRpcProviderConfigBean configBean) {
        return new ScoaRpcProviderBeanProcessor(configBean.getPort());
    }

    @Bean
    public ScoaRpcProviderServer scoaRpcProviderServer(@Autowired ScoaRpcProviderBeanProcessor scoaRpcProviderBeanProcessor,
                                                       @Autowired ScoaRpcProviderConfigBean configBean) {
        return new ScoaRpcProviderServer(scoaRpcProviderBeanProcessor.getPort(), configBean);
    }

}
