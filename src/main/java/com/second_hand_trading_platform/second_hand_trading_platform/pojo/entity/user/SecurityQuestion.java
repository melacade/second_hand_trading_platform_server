package com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.user;

import lombok.Data;

@Data
public class SecurityQuestion {
    private Integer id;
    private String question;
    private String answer;
    private String userID;
    private Long changeTime;
}
