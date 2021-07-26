package me.gleeming.command.paramter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Param {
    /**
     * Name of the parameter
     */
    String name();

    /**
     * Makes it so the rest of arguments in a command
     * are connected together after concatted is found
     */
    boolean concated() default false;

    /**
     * Checks to see if a parameter is required
     */
    boolean required() default true;
}
