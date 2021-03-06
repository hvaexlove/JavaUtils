package com.shaw.note;

import java.lang.annotation.*;

/**
 * Created by Administrator on 2017/2/22 0022.
 *
 * 自动注入注解
 *
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface AutoBody {

    String value() default "";

}
