package com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.goods;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class Comments {
    Integer id;
    String content;
    String userBaseId;
    Integer goodsId;
    Timestamp createdAt;
}
