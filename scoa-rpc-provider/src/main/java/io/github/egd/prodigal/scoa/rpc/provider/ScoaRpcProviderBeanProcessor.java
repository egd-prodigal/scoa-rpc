package io.github.egd.prodigal.scoa.rpc.provider;

import io.github.egd.prodigal.scoa.rpc.annotations.ScoaRpcProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.lang.NonNull;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class ScoaRpcProviderBeanProcessor implements BeanPostProcessor {

    private final Logger logger = LoggerFactory.getLogger(ScoaRpcProviderBeanProcessor.class);

    private final Set<Class<?>> classSet = new HashSet<>();

    private final Integer port;

    public ScoaRpcProviderBeanProcessor(Integer port) {
        this.port = port;
    }

    @Override
    public Object postProcessBeforeInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        if (targetClass.getAnnotation(ScoaRpcProvider.class) != null) {
            classSet.add(targetClass);
        }
        return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
    }

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        if (targetClass.equals(EurekaInstanceConfigBean.class)) {
            if (classSet.isEmpty()) {
                return bean;
            }

            Map<String, List<String>> map = new HashMap<>();
            for (Class<?> aClass : classSet) {
                for (Class<?> clientClass : aClass.getInterfaces()) {
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
            }
            StringBuilder scoaRpcProvider = new StringBuilder();
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                scoaRpcProvider.append(entry.getKey()).append("{");
                List<String> value = entry.getValue();
                scoaRpcProvider.append(String.join(";", value));
                scoaRpcProvider.append("}&");
            }
            String scoaRpcProviderInfo = scoaRpcProvider.substring(0, scoaRpcProvider.length() - 1);
            EurekaInstanceConfigBean eurekaInstanceConfigBean = (EurekaInstanceConfigBean) bean;
            eurekaInstanceConfigBean.getMetadataMap().put("scoa.rpc.provider.info", scoaRpcProviderInfo);
            eurekaInstanceConfigBean.getMetadataMap().put("scoa.rpc.provider.port", String.valueOf(port));
            logger.info("scoa.rpc.provider.info: {}", scoaRpcProviderInfo);
            classSet.clear();
        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }

    public Integer getPort() {
        return port;
    }

}
