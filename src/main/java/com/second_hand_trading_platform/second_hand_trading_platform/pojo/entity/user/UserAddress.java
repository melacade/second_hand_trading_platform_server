package com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.user;

import lombok.Data;

@Data
public class UserAddress {
    Integer id;
    String country;
    String province;
    String city;
    String detail;
    String userBaseId;
    Boolean isDefault;
}
