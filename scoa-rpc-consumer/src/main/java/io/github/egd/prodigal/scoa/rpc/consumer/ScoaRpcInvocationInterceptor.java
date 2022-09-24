package io.github.egd.prodigal.scoa.rpc.consumer;

public interface ScoaRpcInvocationInterceptor {

    void interceptor(ScoaRpcInvocationInterceptorChain.HttpHolder httpHolder);

}
