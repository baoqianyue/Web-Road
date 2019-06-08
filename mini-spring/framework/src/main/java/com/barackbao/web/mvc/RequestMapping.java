package com.barackbao.web.mvc;

import java.lang.annotation.*;

/**
 * Created by barackbao on 2019-06-07
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequestMapping {

    // 保存url信息
    String value();
}
