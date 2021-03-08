package com.second_hand_trading_platform.second_hand_trading_platform.modules.common.dto.input.nomal_query;

import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.user.SecurityQuestion;
import lombok.Data;

import java.util.List;

@Data
public class ValidateProblems{
    String account;
    List<SecurityQuestion> questions;
}
