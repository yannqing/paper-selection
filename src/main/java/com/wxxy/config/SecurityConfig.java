package com.wxxy.config;

import com.wxxy.common.Constant;
import com.wxxy.security.filter.JwtAuthenticationTokenFilter;
import com.wxxy.security.handler.MyLoginFailureHandler;
import com.wxxy.security.handler.MyLoginSuccessHandler;
import com.wxxy.security.handler.MyLogoutSuccessHandler;
import com.wxxy.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    RedisCache redisCache;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((authorize)->authorize
                .requestMatchers(Constant.annos).permitAll()
                .anyRequest()
                .authenticated()
        );
        //关闭session
        http.sessionManagement((session)->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        //设置登录成功处理器
        http.formLogin((login)->login.
                loginProcessingUrl("/login")
                .successHandler(new MyLoginSuccessHandler(redisCache))
                .failureHandler(new MyLoginFailureHandler())
        );

        //设置退出成功处理器
        http.logout((logout)->{
            logout.logoutUrl("/logout")
                    .logoutSuccessHandler(new MyLogoutSuccessHandler(redisCache));
        });

        //设置token过滤器在usernamePassword过滤器之前
        http.addFilterBefore(new JwtAuthenticationTokenFilter(redisCache), UsernamePasswordAuthenticationFilter.class);


        //关闭跨域
        http.cors(Customizer.withDefaults());

        //关闭csrf
        http.csrf(AbstractHttpConfigurer::disable);


        return http.build();
    }

    /**
     * 对密码进行BCrypt加密
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
