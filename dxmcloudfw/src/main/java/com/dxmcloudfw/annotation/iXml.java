package com.dxmcloudfw.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author dongxm
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface  iXml {
    
//    String nodeName() ;  
    boolean req() default true;
    boolean isCData() default false;
    int orderId() ;
    String pNodeName() default "";
    
    
}
