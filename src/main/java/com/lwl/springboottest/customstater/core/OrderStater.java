package com.lwl.springboottest.customstater.core;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface OrderStater {

    int order() default 0;

    String[] methods() default {};
}

