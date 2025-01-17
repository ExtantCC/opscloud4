package com.baiyi.opscloud.leo.annotation;

import java.lang.annotation.*;

/**
 * @Author baiyi
 * @Date 2022/12/29 13:22
 * @Version 1.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface LeoDeployInterceptor {

    public static final boolean DISABLED = false;

    /**
     * 任务ID （SpEL语法）
     * @return
     */
    String jobIdSpEL() default "";


    String deployTypeSpEL() default "";

    /**
     * 允许并发
     * @return
     */
    boolean allowConcurrency() default false;

}
