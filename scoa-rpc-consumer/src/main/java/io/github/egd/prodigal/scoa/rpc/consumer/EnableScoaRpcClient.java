package io.github.egd.prodigal.scoa.rpc.consumer;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(ScoaRpcClientConfiguration.class)
public @interface EnableScoaRpcClient {



}
