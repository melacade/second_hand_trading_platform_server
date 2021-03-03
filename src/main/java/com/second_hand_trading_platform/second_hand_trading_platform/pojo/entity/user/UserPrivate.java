package com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.user;

import lombok.Data;

import java.util.Date;

@Data
public class UserPrivate {
    private Integer id;
    private String userID;
    private Integer level;
    private String password;
    private String account;
    private String salt;
    private String paymentPWD;
    private boolean locked;
}
