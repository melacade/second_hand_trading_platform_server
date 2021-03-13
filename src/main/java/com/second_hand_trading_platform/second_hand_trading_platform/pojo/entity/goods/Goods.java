package com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.goods;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class Goods {
    Integer id;
    String name;
    String info;
    int price;
    String shopId;
    int originalPrice;
    String defaultImage;
    int count;
    int newPercentage;
    Timestamp created_at;
}
