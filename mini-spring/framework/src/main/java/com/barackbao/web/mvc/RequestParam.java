package com.barackbao.web.mvc;

import java.lang.annotation.*;

/**
 * Created by barackbao on 2019-06-07
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface RequestParam {

    // 方法参数名
    String value();
}
