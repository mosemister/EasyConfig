package org.easy.config.auto.annotations;

import org.easy.config.Serializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigField {

    Class<? extends Serializer> serializer() default Serializer.class;

    Class<?> auto() default Object.class;

    boolean exclude() default false;

    String name() default "";

}
