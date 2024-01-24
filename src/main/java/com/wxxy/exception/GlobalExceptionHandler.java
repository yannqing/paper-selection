package com.wxxy.exception;

import com.wxxy.utils.ResultUtils;
import com.wxxy.vo.BaseResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

/**
 * 全局异常处理器
 *
 * @author yannqing
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 权限校验异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public BaseResponse handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址 {},权限校验失败 {}", requestURI, e.getMessage());
        return ResultUtils.failure("没有权限，请联系管理员授权");
    }

    /**
     * 请求方式不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public BaseResponse handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e,
                                                      HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址 {},不支持 {} 请求", requestURI, e.getMethod());
        return ResultUtils.failure(e.getMessage());
    }

    /**
     * 拦截未知的运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public void handleRuntimeException(RuntimeException e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String requestURI = request.getRequestURI();
        log.error("请求地址 {},异常: {}", requestURI, e);

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(500);
        response.getWriter().write(ResultUtils.failure(e.getMessage()).toString());
    }

    /**
     * redis连接异常
     */
    @ExceptionHandler(RedisConnectionFailureException.class)
    public BaseResponse handleRedisConnectionFailureException(RedisConnectionFailureException e, HttpServletRequest request, HttpServletResponse response){
        log.error("redis连接异常："+e.getMessage());
        return ResultUtils.failure("服务器繁忙，请稍后重试！");
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public BaseResponse handleException(Exception e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址 {},发生系统异常.", requestURI, e);
        return ResultUtils.failure(e.getMessage());
    }

}
