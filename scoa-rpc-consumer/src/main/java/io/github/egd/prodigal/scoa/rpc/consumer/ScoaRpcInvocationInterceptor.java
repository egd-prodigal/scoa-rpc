package io.github.egd.prodigal.scoa.rpc.consumer;

import org.springframework.http.client.ClientHttpResponse;

public interface ScoaRpcInvocationInterceptor {

    default void preInterceptor(ScoaRpcInvocationHttpHolder httpHolder) {

    }

    default ClientHttpResponse afterInterceptor(ScoaRpcInvocationHttpHolder httpHolder, ClientHttpResponse response) {
        return response;
    }



}
