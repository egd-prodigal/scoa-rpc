package io.github.egd.prodigal.scoa.rpc.consumer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.netflix.loadbalancer.Server;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ScoaRpcConsumerProxy implements InvocationHandler {

    private final RestTemplate restTemplate;

    private static final Gson gson = new GsonBuilder().create();

    public ScoaRpcConsumerProxy(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class<?> returnType = method.getReturnType();
        // 这里进行微服务调用
        Server server = (Server) args[args.length - 1];
        URI uri = UriComponentsBuilder.newInstance().scheme("http").host(server.getHost()).port(server.getPort()).path("/").build().toUri();
        RequestEntity.BodyBuilder bodyBuilder = RequestEntity.post(uri);
        Class<?>[] parameterTypes = method.getParameterTypes();
        String scoaProvider = method.getDeclaringClass().getName() + "#" + method.getName() + "#"
                + (parameterTypes.length > 0 ? Arrays.stream(parameterTypes).map(Class::getName).collect(Collectors.joining(",")) : "");
        bodyBuilder.header("scoa-provider", scoaProvider);
        RequestEntity<?> requestEntity;
        JsonObject jsonObject = new JsonObject();
        if (args.length > 1) {
            // 有额外参数
            for (int i = 0; i < args.length - 1; i++) {
                Object arg = args[i];
                JsonObject argJson = new JsonObject();
                argJson.addProperty("class", arg.getClass().getName());
                argJson.addProperty("data", gson.toJson(arg));
                jsonObject.add("arg" + i, argJson);
            }
            jsonObject.addProperty("args", args.length - 1);
        } else {
            jsonObject.addProperty("args", 0);
        }
        String body = jsonObject.toString();
        requestEntity = bodyBuilder.body(body);
        ResponseEntity<Resource> responseEntity = restTemplate.exchange(requestEntity, Resource.class);
        HttpStatus statusCode = responseEntity.getStatusCode();
        if (statusCode.is2xxSuccessful() && !"void".equals(returnType.getName())) {
            Resource resource = responseEntity.getBody();
            if (resource != null) {
                try (InputStream inputStream = resource.getInputStream();
                     InputStreamReader reader = new InputStreamReader(inputStream)) {
                    return gson.fromJson(reader, returnType);
                }
            }
        }
        return null;
    }


}
