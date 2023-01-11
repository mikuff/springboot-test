package com.lwl.springboottest.customstater.core;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface OrderStater {

    // 排序
    int order() default 0;

    // 方法列表
    String[] methods() default {};
}

