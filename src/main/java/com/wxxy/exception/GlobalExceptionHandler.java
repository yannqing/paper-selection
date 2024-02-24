package com.wxxy.exception;

import com.wxxy.common.Code;
import com.wxxy.utils.ResultUtils;
import com.wxxy.vo.BaseResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.nio.file.AccessDeniedException;

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
    public BaseResponse<Object> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址 {},权限校验失败 {}", requestURI, e.getMessage());
        return ResultUtils.failure("没有权限，请联系管理员授权");
    }

    /**
     * 请求方式不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public BaseResponse<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e,
                                                      HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址 {},不支持 {} 请求", requestURI, e.getMethod());
        return ResultUtils.failure(e.getMessage());
    }

    /**
     * 身份过期
     */
    @ExceptionHandler(IllegalStateException.class)
    public BaseResponse<Object> handleIllegalStateException(IllegalStateException e,
                                                            HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("用户过期，请重新登录",e.getMessage());
        return ResultUtils.failure(Code.AUTH_ERROR, null, e.getMessage());
    }
    /**
     * 参数错误
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public BaseResponse<Object> handleIllegalArgumentException(IllegalArgumentException e,
                                                    HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("非法参数=>{}",e.getMessage());
        return ResultUtils.failure(e.getMessage());
    }

    @ExceptionHandler(ClassCastException.class)
    public BaseResponse<Object> handleClassCastException(ClassCastException e,
                                                       HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址 {},权限异常: {}", requestURI, e.getMessage());
        return ResultUtils.failure("您没有权限访问此接口，请联系管理员");
    }

    /**
     * 拦截未知的运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<Object> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址 {},异常: {}", requestURI, e);

        return ResultUtils.failure(e.getMessage());
    }

    /**
     * redis连接异常
     */
//    @ExceptionHandler(RedisConnectionFailureException.class)
//    public BaseResponse handleRedisConnectionFailureException(RedisConnectionFailureException e, HttpServletRequest request, HttpServletResponse response){
//        log.error("redis连接异常："+e.getMessage());
//        return ResultUtils.failure("服务器繁忙，请稍后重试！");
//    }
    /**
     * 文件异常
     */
    @ExceptionHandler(IOException.class)
    public BaseResponse<Object> handleIOException(IOException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址 {},发生文件处理异常.", requestURI, e);
        return ResultUtils.failure(e.getMessage());
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public BaseResponse<Object> handleException(Exception e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址 {},发生系统异常.", requestURI, e);
        return ResultUtils.failure(e.getMessage());
    }

}
