package io.github.egd.prodigal.scoa.rpc.provider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StreamUtils;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;

public class ScoaRpcDispatchServlet extends HttpServlet implements ApplicationContextAware {

    private final Gson gson = new GsonBuilder().create();

    private ApplicationContext applicationContext;

    @Override
    protected void service(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response) throws ServletException, IOException {
        String scoaProvider = request.getHeader("scoa-provider");
        String[] providerArray = scoaProvider.split("#");
        String className = providerArray[0];
        String methodName = providerArray[1];
        Object bean = applicationContext.getBean(className);
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        Method method;
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
            method = ReflectionUtils.findMethod(targetClass, methodName, parameterTypes);
        } else {
            method = ReflectionUtils.findMethod(targetClass, methodName);
        }

        ServletInputStream inputStream = request.getInputStream();
        String inputString = StreamUtils.copyToString(inputStream, Charset.defaultCharset());
        JsonObject jsonObject = gson.fromJson(inputString, JsonObject.class);
        int argNumber = jsonObject.get("argNumber").getAsNumber().intValue();
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
                result = AopUtils.invokeJoinpointUsingReflection(bean, method, args);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                result = AopUtils.invokeJoinpointUsingReflection(bean, method, new Object[0]);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        String responseString = gson.toJson(result);
        response.setStatus(HttpStatus.OK.value());
        response.setContentType("application/json");
        response.getWriter().write(responseString);
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
