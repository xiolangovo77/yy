package com.ecnu.aspect;

import com.ecnu.annotation.RequireRole;
import com.ecnu.context.RoleContext;
import com.ecnu.exception.BusinessException;
import com.ecnu.exception.ErrorCode;
import com.ecnu.model.enums.RoleEnum;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色权限校验切面
 * 拦截 @RequireRole 注解的方法，校验当前用户角色是否在允许列表中
 */
@Aspect
@Component
@Slf4j
public class RequireRoleAspect {

    @Pointcut("execution(* com.ecnu.controller.*.*(..)) && @annotation(com.ecnu.annotation.RequireRole)")
    public void requireRolePointCut() {}

    @Before("requireRolePointCut()")
    public void checkRole(JoinPoint joinPoint) {
        // 获取方法上的 @RequireRole 注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        RequireRole requireRole = signature.getMethod().getAnnotation(RequireRole.class);
        RoleEnum[] allowedRoles = requireRole.value();

        // 获取当前用户角色
        Long currentRoleId = RoleContext.getCurrentRoleId();

        // 收集允许的角色编码
        Set<Long> allowedRoleIds = Arrays.stream(allowedRoles)
                .map(RoleEnum::getCode)
                .collect(Collectors.toSet());

        log.info("权限校验 —— 当前角色ID：{}，允许角色：{}", currentRoleId, allowedRoleIds);

        // 当前角色不在允许列表中，拒绝访问
        if (currentRoleId == null || !allowedRoleIds.contains(currentRoleId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
    }
}
