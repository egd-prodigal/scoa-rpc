package io.github.egd.prodigal.scoa.rpc.provider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

public class ScoaRpcProviderHolder {

    private static final Gson gson = new GsonBuilder().create();

    private final Object object;

    private final Method method;

    protected ScoaRpcProviderHolder(String scoaProvider, ApplicationContext applicationContext) {
        String[] providerArray = scoaProvider.split("#");
        String className = providerArray[0];
        String methodName = providerArray[1];
        this.object = applicationContext.getBean(className);
        Class<?> targetClass = AopUtils.getTargetClass(this.object);
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
            this.method = ReflectionUtils.findMethod(targetClass, methodName, parameterTypes);
        } else {
            this.method = ReflectionUtils.findMethod(targetClass, methodName);
        }
    }

    protected Object call(String inputString) {
        JsonObject jsonObject = gson.fromJson(inputString, JsonObject.class);
        int argNumber = jsonObject.get("args").getAsNumber().intValue();
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
                result = AopUtils.invokeJoinpointUsingReflection(this.object, this.method, args);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                result = AopUtils.invokeJoinpointUsingReflection(this.object, this.method, new Object[0]);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

}
