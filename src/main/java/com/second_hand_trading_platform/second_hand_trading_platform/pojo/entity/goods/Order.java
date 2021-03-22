package com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.goods;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class Order {
    String id;
    Integer goodsId;
    String name;
    Double price;
    String baseUserId;
    Integer address;
    Integer count;
    Integer status;
    Timestamp createdAt;
    String image;
}
