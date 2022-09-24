package io.github.egd.prodigal.scoa.rpc.consumer;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

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
            HttpHolder httpHolder = new HttpHolder();
            httpHolder.setRequest(request);
            httpHolder.setBody(body);
            for (ScoaRpcInvocationInterceptor invocationInterceptor : invocationInterceptors) {
                invocationInterceptor.interceptor(httpHolder);
            }
            return execution.execute(httpHolder.request, httpHolder.body);
        }
        return execution.execute(request, body);
    }

    public static class HttpHolder {

        private HttpRequest request;

        private byte[] body;

        public HttpRequest getRequest() {
            return request;
        }

        public void setRequest(HttpRequest request) {
            this.request = request;
        }

        public byte[] getBody() {
            return body;
        }

        public void setBody(byte[] body) {
            this.body = body;
        }
    }

}
