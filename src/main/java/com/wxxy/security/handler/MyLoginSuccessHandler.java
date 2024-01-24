package com.wxxy.security.handler;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wxxy.common.Code;
import com.wxxy.domain.User;
import com.wxxy.utils.JwtUtils;
import com.wxxy.utils.RedisCache;
import com.wxxy.utils.ResultUtils;
import com.wxxy.vo.SecurityUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class MyLoginSuccessHandler implements AuthenticationSuccessHandler {


    private final RedisCache redisCache;

    public MyLoginSuccessHandler(RedisCache redisCache) {
        this.redisCache = redisCache;
    }

    /**
     * 登录成功处理器：返回用户信息，对应用户的权限信息，登录生成token
     * @param request
     * @param response
     * @param authentication
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");

        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        User user = securityUser.getUser();
        ObjectMapper objectMapper = new ObjectMapper();
        String userInfo = objectMapper.writeValueAsString(user);


        //生成token
        String token = JwtUtils.token(userInfo);
        //将token存入redis中
        redisCache.setCacheObject("token:"+token,String.valueOf(authentication),60*60*3, TimeUnit.SECONDS);

        Map<String,Object> map = new HashMap<>();
        map.put("token", token);
        map.put("userInfo",user);

        response.getWriter().write(JSONUtil.toJsonStr(ResultUtils.success(Code.LOGIN_SUCCESS,map,"登录成功")));
        log.info("登录成功！");
    }
}
