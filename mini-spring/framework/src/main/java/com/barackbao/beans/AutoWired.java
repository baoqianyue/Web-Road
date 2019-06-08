package com.barackbao.beans;

import java.lang.annotation.*;

/**
 * Created by barackbao on 2019-06-08
 * 依赖注解
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AutoWired {
}
