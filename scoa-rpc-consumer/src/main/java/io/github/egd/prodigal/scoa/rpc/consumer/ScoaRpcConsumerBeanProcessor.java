package io.github.egd.prodigal.scoa.rpc.consumer;

import io.github.egd.prodigal.scoa.rpc.annotations.ScoaRpcConsumer;
import org.apache.commons.lang.reflect.FieldUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.core.PriorityOrdered;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

public class ScoaRpcConsumerBeanProcessor implements SmartInstantiationAwareBeanPostProcessor, PriorityOrdered {


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        Field[] declaredFields = targetClass.getDeclaredFields();
        Arrays.stream(declaredFields).filter(field -> field.isAnnotationPresent(ScoaRpcConsumer.class)).forEach(field -> {
//            proxyScoaRpcConsumer(field, bean);
        });
        return SmartInstantiationAwareBeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }

    private void proxyScoaRpcConsumer(Field field, Object bean) {
        try {
            Object scoaRpcConsumer = FieldUtils.readDeclaredField(bean, field.getName(), true);
            Object proxyFieldObject;
            proxyFieldObject = proxyByJdk(scoaRpcConsumer);
            FieldUtils.writeField(field, bean, proxyFieldObject, true);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private Object proxyByJdk(Object fieldObject) {
        return Proxy.newProxyInstance(fieldObject.getClass().getClassLoader(), fieldObject.getClass().getInterfaces(), new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return method.invoke(proxy, args);
            }
        });
    }

    private Object proxyByCGLIB(Object fieldObject) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(fieldObject.getClass());
        enhancer.setCallback(new MethodInterceptor() {
            @Override
            public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                return method.invoke(fieldObject, objects);
            }
        });
        return enhancer.create();
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }
}
