package com.second_hand_trading_platform.second_hand_trading_platform.config.security.filter;

import com.alibaba.fastjson.JSONObject;
import com.second_hand_trading_platform.second_hand_trading_platform.config.security.handler.AdminAuthenticationFailureHandler;
import com.second_hand_trading_platform.second_hand_trading_platform.config.security.Constants;
import com.second_hand_trading_platform.second_hand_trading_platform.config.security.manager.CusAuthenticationManager;
import com.second_hand_trading_platform.second_hand_trading_platform.config.security.handler.AdminAuthenticationSuccessHandler;
import com.second_hand_trading_platform.second_hand_trading_platform.utils.MultiReadHttpServletRequest;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Component
public class AdminAuthenticationProcessingFilter extends AbstractAuthenticationProcessingFilter {

    /**
     * @param authenticationManager:             认证管理器
     * @param adminAuthenticationSuccessHandler: 认证成功处理
     * @param adminAuthenticationFailureHandler: 认证失败处理
     */
    public AdminAuthenticationProcessingFilter(CusAuthenticationManager authenticationManager, AdminAuthenticationSuccessHandler adminAuthenticationSuccessHandler, AdminAuthenticationFailureHandler adminAuthenticationFailureHandler) {
        super(new AntPathRequestMatcher("/login", "POST"));
        this.setAuthenticationManager(authenticationManager);
        this.setAuthenticationSuccessHandler(adminAuthenticationSuccessHandler);
        this.setAuthenticationFailureHandler(adminAuthenticationFailureHandler);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (request.getContentType() == null || !request.getContentType().contains(Constants.REQUEST_HEADERS_CONTENT_TYPE)) {
            throw new AuthenticationServiceException("请求头类型不支持: " + request.getContentType());
        }

        UsernamePasswordAuthenticationToken authRequest;
        try {
            MultiReadHttpServletRequest wrappedRequest = new MultiReadHttpServletRequest(request);
            // 将前端传递的数据转换成jsonBean数据格式
            Map<String,String> user = JSONObject.parseObject(wrappedRequest.getBodyJsonStrByJson(wrappedRequest), HashMap.class);
            String token = user.get("token");
            HashMap<String, String> pwd = new HashMap<>();
            pwd.put("token",token);
            pwd.put("password",user.get("password"));
            authRequest = new UsernamePasswordAuthenticationToken(user.get("username"), pwd, null);
            authRequest.setDetails(authenticationDetailsSource.buildDetails(wrappedRequest));
        } catch (Exception e) {
            throw new AuthenticationServiceException(e.getMessage());
        }
        return this.getAuthenticationManager().authenticate(authRequest);
    }
}