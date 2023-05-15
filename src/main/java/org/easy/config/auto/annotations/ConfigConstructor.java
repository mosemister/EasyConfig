package org.easy.config.auto.annotations;

import org.easy.config.Serializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.CONSTRUCTOR)
public @interface ConfigConstructor {

    Class<? extends Serializer>[] serializers() default Serializer.class;
}
