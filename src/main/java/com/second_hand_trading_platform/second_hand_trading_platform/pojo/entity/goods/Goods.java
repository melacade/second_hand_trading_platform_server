package com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.goods;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class Goods {
    Integer id;
    String name;
    String info;
    Double price;
    String ownerId;
    Double originalPrice;
    String defaultImage;
    int count;
    int newPercentage;
    Timestamp createdAt;
}
