package com.lab.management.config;

import com.lab.management.common.Result;
import com.lab.management.exception.BusinessException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result handleBusinessException(BusinessException ex) {
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleValidationException(MethodArgumentNotValidException ex) {
        return Result.error("请求参数校验失败");
    }

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception ex) {
        return Result.error("系统异常，请稍后重试");
    }
}
