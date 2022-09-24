package io.github.egd.prodigal.scoa.rpc.consumer;

import com.netflix.loadbalancer.Server;
import io.github.egd.prodigal.scoa.rpc.annotations.ScoaRpcConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ScoaRpcConsumerServiceChooser implements InvocationHandler {

    private final Logger logger = LoggerFactory.getLogger(ScoaRpcConsumerServiceChooser.class);

    private final InvocationHandler invocationHandle;

    private final String version;

    private final String group;

    private final ScoaRpcConsumerServiceHolder serviceHolder;

    public ScoaRpcConsumerServiceChooser(InvocationHandler invocationHandle, ScoaRpcConsumer scoaRpcConsumer,
                                         ScoaRpcConsumerServiceHolder scoaRpcConsumerServiceHolder) {
        this.invocationHandle = invocationHandle;
        this.serviceHolder = scoaRpcConsumerServiceHolder;
        this.version = scoaRpcConsumer.version();
        this.group = scoaRpcConsumer.group();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        if ("toString".equals(methodName)) {
            return AopUtils.getTargetClass(proxy).getName();
        }
        Class<?> declaringClass = method.getDeclaringClass();
        String packageName = declaringClass.getPackage().getName();
        String className = declaringClass.getSimpleName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        Server server;
        if (parameterTypes.length > 0) {
            String[] parameterTypeNames = new String[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                parameterTypeNames[i] = parameterTypes[i].getName();
            }
            server = serviceHolder.chooseServer(packageName, className, version, group, methodName, parameterTypeNames);
        } else {
            server = serviceHolder.chooseServer(packageName, className, version, group, methodName);
        }
        logger.debug("will send rpc to service: {}", server.getId());
        Object[] fillArgs = args == null ? new Object[1] : new Object[args.length + 1];
        if (args != null && args.length > 0) {
            System.arraycopy(args, 0, fillArgs, 0, args.length);
        }
        fillArgs[fillArgs.length - 1] = server;
        return invocationHandle.invoke(proxy, method, fillArgs);
    }

}
