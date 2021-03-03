package com.second_hand_trading_platform.second_hand_trading_platform.config.security.provider;

import com.second_hand_trading_platform.second_hand_trading_platform.mybatis.UserDAO;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.user.User;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.user.UserBaseInfo;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.user.UserPrivate;
import com.second_hand_trading_platform.second_hand_trading_platform.service.UserService;
import com.second_hand_trading_platform.second_hand_trading_platform.utils.PasswordUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AdminAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    UserService userDetailsService;
    @Autowired
    UserDAO userDAO;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // 获取前端表单中输入后返回的用户名、密码
        String userName = (String) authentication.getPrincipal();
        Map<String,String> pwd = (Map) authentication.getCredentials();
        boolean isValidToken = false;
        boolean isValid = false;
        User userDetails;
        if(Strings.isNotBlank(pwd.get("token"))){
            userDetails =  userDetailsService.getUserByToken(pwd.get("token"));
            isValidToken = PasswordUtils.isValidToken(userDetails.getUserBaseInfo().getToken(),pwd.get("token"),userDetails.getUserBaseInfo().getAuthTime());
        }else{
            userDetails =(User) userDetailsService.loadUserByUsername(userName);
            isValid = PasswordUtils.isValidPassword(pwd.get("password"), userDetails.getPassword(), userDetails.getUserPrivate().getSalt());
        }



        // 验证密码
        if (!isValid&&!isValidToken) {
            throw new BadCredentialsException("密码错误！");
        }

        // 前后端分离情况下 处理逻辑...
        // 更新登录令牌 - 之后访问系统其它接口直接通过token认证用户权限...
        if(!isValidToken){
            long authTime = System.currentTimeMillis();
            String token = PasswordUtils.encodePassword( authTime+ userDetails.getUserPrivate().getSalt(), userDetails.getUserPrivate().getSalt());
            UserBaseInfo user= userDAO.loadUserBaseInfoByUserID(userDetails.getUserID());
            user.setToken(token);
            user.setAuthTime(authTime);

            userDAO.updateUserBaseInfo(user);
            userDetails.setUserBaseInfo(user);
        }
        return new UsernamePasswordAuthenticationToken(userDetails, pwd.get("password"), userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return true;
    }
}