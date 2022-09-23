package io.github.egd.prodigal.scoa.rpc.consumer;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Proxy;

public class ScoaRpcConsumerFactory implements FactoryBean<Object>, ApplicationContextAware {

    private final Class<?> type;

    private RestTemplate restTemplate;

    public ScoaRpcConsumerFactory(Class<?> type) {
        this.type = type;
    }

    @Override
    public Object getObject() throws Exception {
        ScoaRpcConsumerProxy scoaRpcConsumerProxy = new ScoaRpcConsumerProxy(restTemplate);
        return Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, scoaRpcConsumerProxy);
    }

    @Override
    public Class<?> getObjectType() {
        return type;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.restTemplate = applicationContext.getBean("scoaRpcConsumerRestTemplate", RestTemplate.class);
    }

}
