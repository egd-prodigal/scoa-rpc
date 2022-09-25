package io.github.egd.prodigal.scoa.rpc.consumer;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.List;

public class ScoaRpcInvocationInterceptorChain implements ClientHttpRequestInterceptor {


    private final List<ScoaRpcInvocationInterceptor> invocationInterceptors;

    public ScoaRpcInvocationInterceptorChain(List<ScoaRpcInvocationInterceptor> invocationInterceptors) {
        this.invocationInterceptors = invocationInterceptors;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        if (invocationInterceptors != null) {
            ScoaRpcInvocationHttpHolder httpHolder = new ScoaRpcInvocationHttpHolder();
            httpHolder.setRequest(request);
            httpHolder.setBody(body);
            for (ScoaRpcInvocationInterceptor invocationInterceptor : invocationInterceptors) {
                invocationInterceptor.preInterceptor(httpHolder);
            }
            ClientHttpResponse response = execution.execute(httpHolder.getRequest(), httpHolder.getBody());
            for (ScoaRpcInvocationInterceptor invocationInterceptor : invocationInterceptors) {
                response = invocationInterceptor.afterInterceptor(httpHolder, response);
            }
            return response;
        }
        return execution.execute(request, body);
    }


}
