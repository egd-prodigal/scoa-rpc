package io.github.egd.prodigal.scoa.rpc.provider;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({ScoaRpcProviderRegister.class, ScoaRpcProviderConfiguration.class})
public @interface EnableScoaRpcProvider {

    String[] basePackages() default {};

}
