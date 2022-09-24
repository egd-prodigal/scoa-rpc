package io.github.egd.prodigal.scoa.rpc.provider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.undertow.connector.ByteBufferPool;
import io.undertow.server.XnioByteBufferPool;
import io.undertow.server.protocol.http.HttpOpenListener;
import io.undertow.util.HeaderMap;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.util.ReflectionUtils;
import org.xnio.*;
import org.xnio.channels.AcceptingChannel;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ScoaRpcProviderServer implements ApplicationRunner, ApplicationContextAware {

    private final Logger logger = LoggerFactory.getLogger(ScoaRpcProviderServer.class);

    private ApplicationContext applicationContext;

    private final Integer port;

    private static final Gson gson = new GsonBuilder().create();

    private final Map<String, ScoaRpcProviderHolder> providerHolderMap = new ConcurrentHashMap<>();

    public ScoaRpcProviderServer(Integer port) {
        this.port = port;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Xnio xnio = Xnio.getInstance();
        XnioWorker worker = xnio.createWorker(OptionMap.builder()
                .set(Options.WORKER_IO_THREADS, 1024)
                .set(Options.WORKER_TASK_CORE_THREADS, 200)
                .set(Options.WORKER_TASK_MAX_THREADS, 800)
                .set(Options.TCP_NODELAY, true)
                .getMap());

        OptionMap socketOptions = OptionMap.builder()
                .set(Options.WORKER_IO_THREADS, 1024)
                .set(Options.TCP_NODELAY, true)
                .set(Options.REUSE_ADDRESSES, true)
                .getMap();

        Pool<ByteBuffer> buffers = new ByteBufferSlicePool(BufferAllocator.DIRECT_BYTE_BUFFER_ALLOCATOR, 4096, 4096 * 200);

        ByteBufferPool byteBufferPool = new XnioByteBufferPool(buffers);
        HttpOpenListener openListener = new HttpOpenListener(byteBufferPool);
        openListener.setRootHandler(httpServerExchange -> {
            HeaderMap requestHeaders = httpServerExchange.getRequestHeaders();
            HeaderValues scoaRpcProvider = requestHeaders.get("scoa-provider");
            String rpcProvider = scoaRpcProvider.getFirst();
            final ScoaRpcProviderHolder rpcProviderHolder;
            if (!providerHolderMap.containsKey(rpcProvider)) {
                rpcProviderHolder = new ScoaRpcProviderHolder(rpcProvider, this.applicationContext);
                providerHolderMap.put(rpcProvider, rpcProviderHolder);
            } else {
                rpcProviderHolder = providerHolderMap.get(rpcProvider);
            }
            httpServerExchange.getRequestReceiver().receiveFullString((exchange, inputString) -> {
                Object result = rpcProviderHolder.call(inputString);
                String responseString = gson.toJson(result);
                exchange.setStatusCode(HttpStatus.OK.value());
                HeaderMap responseHeaders = exchange.getResponseHeaders();
                responseHeaders.add(Headers.CONTENT_TYPE, "application/json");
                exchange.getResponseSender().send(responseString);
            });
        });
        ChannelListener<AcceptingChannel<StreamConnection>> acceptListener = ChannelListeners.openListenerAdapter(openListener);
        EurekaInstanceConfigBean eurekaInstanceConfigBean = applicationContext.getBean(EurekaInstanceConfigBean.class);
        String ipAddress = eurekaInstanceConfigBean.getIpAddress();
        InetSocketAddress bindAddress = new InetSocketAddress(ipAddress, port);
        logger.info("undertow server start at: {}", port);
        AcceptingChannel<? extends StreamConnection> server = worker.createStreamConnectionServer(bindAddress, acceptListener, socketOptions);
        server.resumeAccepts();
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    static class ScoaRpcProviderHolder {

        private final Object object;

        private final Method method;

        private ScoaRpcProviderHolder(String scoaProvider, ApplicationContext applicationContext) {
            String[] providerArray = scoaProvider.split("#");
            String className = providerArray[0];
            String methodName = providerArray[1];
            this.object = applicationContext.getBean(className);
            Class<?> targetClass = AopUtils.getTargetClass(this.object);
            if (providerArray.length == 3 && !"".equals(providerArray[2])) {
                String parameterTypeString = providerArray[2];
                String[] parameterTypeArray = parameterTypeString.split(",");
                Class<?>[] parameterTypes = new Class[parameterTypeArray.length];
                for (int i = 0; i < parameterTypeArray.length; i++) {
                    try {
                        parameterTypes[i] = ClassUtils.getClass(parameterTypeArray[i]);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
                this.method = ReflectionUtils.findMethod(targetClass, methodName, parameterTypes);
            } else {
                this.method = ReflectionUtils.findMethod(targetClass, methodName);
            }
        }

        protected Object call(String inputString) {
            JsonObject jsonObject = gson.fromJson(inputString, JsonObject.class);
            int argNumber = jsonObject.get("args").getAsNumber().intValue();
            Object result;
            if (argNumber > 0) {
                Object[] args = new Object[argNumber];
                for (int i = 0; i < argNumber; i++) {
                    JsonObject argJson = jsonObject.getAsJsonObject("arg" + i);
                    JsonElement argClassName = argJson.get("class");
                    JsonElement argData = argJson.get("data");
                    String argJsonString = argData.getAsString();
                    try {
                        args[i] = gson.fromJson(argJsonString, ClassUtils.getClass(argClassName.getAsString()));
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
                try {
                    result = AopUtils.invokeJoinpointUsingReflection(this.object, this.method, args);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    result = AopUtils.invokeJoinpointUsingReflection(this.object, this.method, new Object[0]);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
            return result;
        }

    }

}
