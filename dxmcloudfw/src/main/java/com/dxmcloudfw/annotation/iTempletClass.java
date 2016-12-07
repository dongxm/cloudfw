/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dxmcloudfw.annotation;

import com.dxmcloudfw.sysenum.TempletEnum;
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
public @interface iTempletClass {

    TempletEnum pojoType();// 请求类型 req  ,  res

    String version();

    TempletEnum templetType();// 模板类型 xml , form 

}
