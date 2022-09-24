package io.github.egd.prodigal.scoa.rpc.provider;

import io.github.egd.prodigal.scoa.rpc.annotations.ScoaRpcProvider;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class ScoaRpcProviderBeanProcessor implements BeanPostProcessor {

    private final Set<Class<?>> list = new HashSet<>();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        if (targetClass.getAnnotation(ScoaRpcProvider.class) != null) {
            list.add(targetClass);
        }
        return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        if (targetClass.equals(EurekaInstanceConfigBean.class)) {
            EurekaInstanceConfigBean eurekaInstanceConfigBean = (EurekaInstanceConfigBean) bean;
            Map<String, List<String>> map = new HashMap<>();
            for (Class<?> aClass : list) {
                Class<?> clientClass = aClass.getInterfaces()[0];
                String packageName = clientClass.getPackage().getName();
                if (!map.containsKey(packageName)) {
                    map.put(packageName, new ArrayList<>());
                }
                List<String> classes = map.get(packageName);

                StringBuilder sb = new StringBuilder();
                String simpleName = clientClass.getSimpleName();
                sb.append(simpleName).append(":");
                ScoaRpcProvider scoaRpcProvider = aClass.getAnnotation(ScoaRpcProvider.class);
                String version = scoaRpcProvider.version();
                sb.append(version).append(":");
                String group = scoaRpcProvider.group();
                sb.append(group).append("[");
                Method[] methods = clientClass.getDeclaredMethods();
                for (int i = 0; i < methods.length; i++) {
                    Method method = methods[i];
                    sb.append(method.getName());
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length > 0) {
                        sb.append("(");
                        sb.append(Arrays.stream(parameterTypes).map(Class::getName).collect(Collectors.joining(",")));
                        sb.append(")");
                    }
                    if (i < methods.length - 1) {
                        sb.append("#");
                    }
                }
                sb.append("]");
                classes.add(sb.toString());
            }
            StringBuilder scoaRpcProvider = new StringBuilder();
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                scoaRpcProvider.append(entry.getKey()).append("{");
                List<String> value = entry.getValue();
                scoaRpcProvider.append(String.join(";", value));
                scoaRpcProvider.append("}&");
            }
            eurekaInstanceConfigBean.getMetadataMap().put("scoa.rpc.provider", scoaRpcProvider.substring(0, scoaRpcProvider.length() - 1));
        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}
