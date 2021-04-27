package com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.goods;

import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class Goods {
    Integer id;
    String name;
    String info;
    Double price;
    String ownerId;
    Double originalPrice;
    String defaultImage;
    List<Integer> labels;
    int count;
    int sales;
    int newPercentage;
    Timestamp createdAt;
}
