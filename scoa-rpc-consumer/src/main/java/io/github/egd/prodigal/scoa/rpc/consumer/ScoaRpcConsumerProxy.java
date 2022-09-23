package io.github.egd.prodigal.scoa.rpc.consumer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import java.util.HashMap;
import java.util.Map;

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
        String instanceName = (String) args[args.length - 3];
        String version = (String) args[args.length - 2];
        String group = (String) args[args.length - 1];
        URI uri = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(instanceName)
                .path("/scoa-rpc")
                .build()
                .toUri();
        RequestEntity.BodyBuilder bodyBuilder = RequestEntity.post(uri);
        String scoaProvider = method.getDeclaringClass().getName() + "#" + method.getName() + ":" + group + ":" + version;
        bodyBuilder.header("scoa-provider", scoaProvider);
        RequestEntity<?> requestEntity;
        if (args.length > 3) {
            // 有额外参数
            Map<String, Object> map = new HashMap<>();
            for (int i = 0; i < args.length - 3; i++) {
                map.put("arg" + i, args[i]);
            }
            requestEntity = bodyBuilder.body(map);
        } else {
            requestEntity = bodyBuilder.build();
        }
        ResponseEntity<Resource> responseEntity = restTemplate.exchange(requestEntity, Resource.class);
        HttpStatus statusCode = responseEntity.getStatusCode();
        if (statusCode.is2xxSuccessful()) {
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
