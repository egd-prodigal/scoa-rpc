package io.github.egd.prodigal.scoa.rpc.consumer;

import org.springframework.http.HttpRequest;

import java.util.HashMap;
import java.util.Map;

public class ScoaRpcInvocationHttpHolder {

    private final Map<String, Object> context = new HashMap<>();

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

    public Object getContext(String key) {
        return context.get(key);
    }

    public void addContext(String key, Object object) {
        this.context.put(key, object);
    }

}
