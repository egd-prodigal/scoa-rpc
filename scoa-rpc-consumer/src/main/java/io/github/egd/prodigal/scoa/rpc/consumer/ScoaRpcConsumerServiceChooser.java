package io.github.egd.prodigal.scoa.rpc.consumer;

import io.github.egd.prodigal.scoa.rpc.annotations.ScoaRpcConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ScoaRpcConsumerServiceChooser implements InvocationHandler {

    private final Logger logger = LoggerFactory.getLogger(ScoaRpcConsumerServiceChooser.class);

    private final InvocationHandler invocationHandle;

    private final ScoaRpcConsumer scoaRpcConsumerAnnotation;

    public ScoaRpcConsumerServiceChooser(InvocationHandler invocationHandle, ScoaRpcConsumer scoaRpcConsumer) {
        this.invocationHandle = invocationHandle;
        this.scoaRpcConsumerAnnotation = scoaRpcConsumer;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String name = method.getName();
        if ("toString".equals(name)) {
            return "";
        }

        Object[] fillArgs = args == null ? new Object[3] : new Object[args.length + 3];
        if (args != null && args.length > 0) {
            System.arraycopy(args, 0, fillArgs, 0, args.length);
        }
        fillArgs[fillArgs.length - 3] = "sample-provider";
        fillArgs[fillArgs.length - 2] = scoaRpcConsumerAnnotation.version();
        fillArgs[fillArgs.length - 1] = scoaRpcConsumerAnnotation.group();
        return invocationHandle.invoke(proxy, method, fillArgs);
    }

}
