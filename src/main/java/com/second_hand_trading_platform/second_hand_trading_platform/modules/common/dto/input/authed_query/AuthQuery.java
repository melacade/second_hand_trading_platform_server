package com.second_hand_trading_platform.second_hand_trading_platform.modules.common.dto.input.authed_query;

import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.user.User;
import lombok.Data;
import org.springframework.security.core.context.SecurityContextHolder;

@Data
abstract class AuthQuery {
    private User user;
    {
        user = (User) SecurityContextHolder.getContext().getAuthentication() .getPrincipal();
    }
}
