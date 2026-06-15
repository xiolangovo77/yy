package com.ecnu.exception;

import com.ecnu.common.BaseResponse;
import com.ecnu.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public BaseResponse<?> httpMessageNotReadableExceptionHandler(HttpMessageNotReadableException e) {
        log.error("HttpMessageNotReadableException", e);
        return ResultUtils.error(ErrorCode.PARAMS_ERROR, "格式错误");
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public BaseResponse<?> duplicateKeyExceptionHandler(DuplicateKeyException e) {
        log.error("DuplicateKeyException", e);
        // 一行提取重复值：按单引号分割，取第二个元素就是重复的用户名
        String duplicateKey = Objects.requireNonNull(e.getRootCause()).getMessage().split("'")[1];
        return ResultUtils.error(ErrorCode.PARAMS_ERROR, duplicateKey + "已存在");
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }
}

