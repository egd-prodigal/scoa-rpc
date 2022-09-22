package io.github.egd.prodigal.scoa.rpc.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ScoaRpcProvider {

    String version() default "1.0.0";

    String group() default "default";

}
