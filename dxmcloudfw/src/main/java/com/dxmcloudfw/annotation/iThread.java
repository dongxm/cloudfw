package com.dxmcloudfw.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author dongxm
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface iThread {

    int loopNumber() default 1;// 默认1次,  -1 为无限次

    long delay() default 1000*5;//间隔时间or固定周期. 对应mode. 单位毫秒 默认5秒
    
    long initialDelay() default 0;//延迟执行时间. 意思是在线程启动多少时间后开始执行. 单位毫秒

    String startTime() default "";//用作固定时间控制. 格式; HH:mm:ss
    
    int mode() default 1;//线程执行方式. 1 延迟执行 . 2 固定周期执行 . 
}
