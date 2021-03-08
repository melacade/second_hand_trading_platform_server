package com.second_hand_trading_platform.second_hand_trading_platform.config.security;


import com.second_hand_trading_platform.second_hand_trading_platform.config.security.filter.AdminAuthenticationProcessingFilter;
import com.second_hand_trading_platform.second_hand_trading_platform.config.security.handler.AdminLogoutSuccessHandler;
import com.second_hand_trading_platform.second_hand_trading_platform.config.security.filter.MyAuthenticationFilter;
import com.second_hand_trading_platform.second_hand_trading_platform.mybatis.UserDAO;
import com.second_hand_trading_platform.second_hand_trading_platform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;


@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private UserService userService;
    @Autowired
    private UserDAO userDAO;
    /**
     * 访问鉴权 - 认证token、签名...
     */
    private final MyAuthenticationFilter myAuthenticationFilter;
    private final AdminAuthenticationProcessingFilter adminAuthenticationProcessingFilter;

    public WebSecurityConfig(AdminAuthenticationProcessingFilter adminAuthenticationProcessingFilter, MyAuthenticationFilter myAuthenticationFilter) {
        this.adminAuthenticationProcessingFilter = adminAuthenticationProcessingFilter;
        this.myAuthenticationFilter = myAuthenticationFilter;
    }

    // 指定密码的加密方式
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 配置用户及其对应的角色
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService);
    }

    // 配置基于内存的 URL 访问权限
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests() // 开启 HttpSecurity 配置
                .antMatchers("/login","/index", "/api/user/register","/api/user/checkSecuriy","/api/user/resetPassword","/api/user/validateProblems","/static/**").permitAll()
                .antMatchers(HttpMethod.OPTIONS, "/**").denyAll()
                .antMatchers("/api/user/**").authenticated()
                .and().headers().frameOptions().disable()
                .and().logout().logoutUrl("/logout").logoutSuccessHandler(new AdminLogoutSuccessHandler(userDAO))
                .and().rememberMe()
                .and().csrf().disable().cors();
        http.addFilterAt(adminAuthenticationProcessingFilter, UsernamePasswordAuthenticationFilter.class).addFilterBefore(myAuthenticationFilter, BasicAuthenticationFilter.class);

    }

    /**
     * 忽略拦截url或静态资源文件夹 - web.ignoring(): 会直接过滤该url - 将不会经过Spring Security过滤器链
     *                             http.permitAll(): 不会绕开springsecurity验证，相当于是允许该路径通过
     * @param web
     * @throws Exception
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(HttpMethod.GET,
                "/favicon.ico",
                "/static/**",
                "/**/*.ttf",
                "/*.html",
                "/**/*.css",
                "/**/*.js");
    }
}
