package io.github.egd.prodigal.scoa.rpc.provider;

import org.apache.commons.lang3.RandomUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "scoa.rpc.provider")
public class ScoaRpcProviderConfigBean {

    private int port = RandomUtils.nextInt(2000, 5000);

    private int workerIoThreads = 1024;

    private int taskCoreThreads = 200;

    private int taskMaxThreads = 800;

    private int socketIoThreads = 1024;

    private int bufferSize = 4096;

    private int regionSize = 200;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getWorkerIoThreads() {
        return workerIoThreads;
    }

    public void setWorkerIoThreads(int workerIoThreads) {
        this.workerIoThreads = workerIoThreads;
    }

    public int getTaskCoreThreads() {
        return taskCoreThreads;
    }

    public void setTaskCoreThreads(int taskCoreThreads) {
        this.taskCoreThreads = taskCoreThreads;
    }

    public int getTaskMaxThreads() {
        return taskMaxThreads;
    }

    public void setTaskMaxThreads(int taskMaxThreads) {
        this.taskMaxThreads = taskMaxThreads;
    }

    public int getSocketIoThreads() {
        return socketIoThreads;
    }

    public void setSocketIoThreads(int socketIoThreads) {
        this.socketIoThreads = socketIoThreads;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public int getRegionSize() {
        return regionSize;
    }

    public void setRegionSize(int regionSize) {
        this.regionSize = regionSize;
    }
}
