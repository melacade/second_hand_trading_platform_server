package com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.user;

import lombok.Data;

@Data
public class Role {
    private Integer id;
    private String userBaseID;
    private String roleID;
    private BaseRole baseRole;
}
