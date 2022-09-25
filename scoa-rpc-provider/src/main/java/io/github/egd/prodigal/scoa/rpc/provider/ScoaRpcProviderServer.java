package io.github.egd.prodigal.scoa.rpc.provider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.undertow.connector.ByteBufferPool;
import io.undertow.server.XnioByteBufferPool;
import io.undertow.server.protocol.http.HttpOpenListener;
import io.undertow.util.HeaderMap;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.xnio.*;
import org.xnio.channels.AcceptingChannel;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ScoaRpcProviderServer implements ApplicationRunner, ApplicationContextAware {

    private final Logger logger = LoggerFactory.getLogger(ScoaRpcProviderServer.class);

    private ApplicationContext applicationContext;

    private final Integer port;

    private final ScoaRpcProviderConfigBean configBean;

    private static final Gson gson = new GsonBuilder().create();

    private final Map<String, ScoaRpcProviderHolder> providerHolderMap = new ConcurrentHashMap<>();

    public ScoaRpcProviderServer(Integer port, ScoaRpcProviderConfigBean configBean) {
        this.port = port;
        this.configBean = configBean;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Xnio xnio = Xnio.getInstance();
        XnioWorker worker = xnio.createWorker(OptionMap.builder()
                .set(Options.WORKER_IO_THREADS, configBean.getWorkerIoThreads())
                .set(Options.WORKER_TASK_CORE_THREADS, configBean.getTaskCoreThreads())
                .set(Options.WORKER_TASK_MAX_THREADS, configBean.getTaskMaxThreads())
                .set(Options.TCP_NODELAY, true)
                .getMap());

        OptionMap socketOptions = OptionMap.builder()
                .set(Options.WORKER_IO_THREADS, configBean.getSocketIoThreads())
                .set(Options.TCP_NODELAY, true)
                .set(Options.REUSE_ADDRESSES, true)
                .getMap();

        int bufferSize = configBean.getBufferSize();
        int regionSize = configBean.getRegionSize();
        Pool<ByteBuffer> buffers = new ByteBufferSlicePool(BufferAllocator.DIRECT_BYTE_BUFFER_ALLOCATOR, bufferSize, regionSize * bufferSize);

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
        logger.info("provider will start in undertow at port: {}", port);
        worker.createStreamConnectionServer(bindAddress, acceptListener, socketOptions).resumeAccepts();
        logger.info("provider started");
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
