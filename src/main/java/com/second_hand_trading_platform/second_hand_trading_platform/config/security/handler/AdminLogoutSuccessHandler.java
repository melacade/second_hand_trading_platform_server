package com.second_hand_trading_platform.second_hand_trading_platform.config.security.handler;

import com.second_hand_trading_platform.second_hand_trading_platform.modules.common.dto.output.ApiResult;
import com.second_hand_trading_platform.second_hand_trading_platform.mybatis.UserDAO;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.user.User;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.user.UserBaseInfo;
import com.second_hand_trading_platform.second_hand_trading_platform.utils.PasswordUtils;
import com.second_hand_trading_platform.second_hand_trading_platform.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AdminLogoutSuccessHandler implements LogoutSuccessHandler {









    @Autowired
    UserDAO userDAO;

    public AdminLogoutSuccessHandler(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
        User user;
        ApiResult result = ApiResult.ok("登出成功！");
        if (authentication != null && (user = (User) authentication.getPrincipal()) != null) {
            long authTime = System.currentTimeMillis();
            String token = PasswordUtils.encodePassword(authTime + user.getUserPrivate().getSalt(), user.getUserPrivate().getSalt());
            UserBaseInfo userBase = userDAO.loadUserBaseInfoByUserID(user.getUserID());
            userBase.setToken(token);
            userBase.setAuthTime(authTime);
            userDAO.updateUserBaseInfo(userBase);

        }
        ResponseUtils.out(httpServletResponse, result);
    }
}
