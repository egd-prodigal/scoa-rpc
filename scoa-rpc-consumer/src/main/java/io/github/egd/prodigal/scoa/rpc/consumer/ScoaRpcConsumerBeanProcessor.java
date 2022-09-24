package io.github.egd.prodigal.scoa.rpc.consumer;

import io.github.egd.prodigal.scoa.rpc.annotations.ScoaRpcConsumer;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.PriorityOrdered;
import org.springframework.lang.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Arrays;

public class ScoaRpcConsumerBeanProcessor implements SmartInstantiationAwareBeanPostProcessor, PriorityOrdered, ApplicationContextAware {

    private final Logger logger = LoggerFactory.getLogger(ScoaRpcConsumerBeanProcessor.class);

    private ApplicationContext applicationContext;

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        Field[] declaredFields = targetClass.getDeclaredFields();
        Arrays.stream(declaredFields)
                .filter(field -> field.isAnnotationPresent(ScoaRpcConsumer.class))
                .forEach(field -> proxyScoaRpcConsumer(field, bean));
        return SmartInstantiationAwareBeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }

    private void proxyScoaRpcConsumer(Field field, Object bean) {
        try {
            Class<?> fieldType = field.getType();
            Object scoaRpcConsumer = this.applicationContext.getBean(fieldType);
            ScoaRpcConsumer rpcConsumerAnnotation = field.getAnnotation(ScoaRpcConsumer.class);
            InvocationHandler invocationHandler = Proxy.getInvocationHandler(scoaRpcConsumer);
            Object proxyFieldObject = proxyJdk(fieldType, invocationHandler, rpcConsumerAnnotation);
            FieldUtils.writeField(field, bean, proxyFieldObject, true);
        } catch (IllegalAccessException e) {
            logger.error("proxyScoaRpcConsumer, field: " + field, e);
        }
    }

    private Object proxyJdk(Class<?> type, InvocationHandler invocationHandle, ScoaRpcConsumer rpcConsumerAnnotation) {
        ScoaRpcConsumerServiceHolder scoaRpcConsumerServiceHolder = applicationContext.getBean(ScoaRpcConsumerServiceHolder.class);
        ScoaRpcConsumerServiceChooser serviceChooser = new ScoaRpcConsumerServiceChooser(invocationHandle, rpcConsumerAnnotation, scoaRpcConsumerServiceHolder);
        return Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, serviceChooser);
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
