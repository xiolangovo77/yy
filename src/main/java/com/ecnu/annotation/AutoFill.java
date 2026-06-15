package com.ecnu.annotation;


import com.ecnu.model.enums.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，用户标识某个方法需要进行功能字段自动填充处理
 */
@Target(ElementType.METHOD) //只用在方法
@Retention(RetentionPolicy.RUNTIME) //运行时保留注释
public @interface AutoFill {
    //数据库操作类型
    OperationType value();
}
