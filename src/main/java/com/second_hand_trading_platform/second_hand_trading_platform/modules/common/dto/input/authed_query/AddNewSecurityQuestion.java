package com.second_hand_trading_platform.second_hand_trading_platform.modules.common.dto.input.authed_query;

import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.user.SecurityQuestion;
import lombok.Data;

import java.util.List;

@Data
public class AddNewSecurityQuestion extends AuthQuery{
    private List<SecurityQuestion> questions;
    private String password;
}
