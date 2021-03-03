package com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.user;

import lombok.Data;


@Data
public class UserBaseInfo {
    private String id;
    private String phone;
    private String avator;
    private long authTime;
    private String name;
    private String token;
}
