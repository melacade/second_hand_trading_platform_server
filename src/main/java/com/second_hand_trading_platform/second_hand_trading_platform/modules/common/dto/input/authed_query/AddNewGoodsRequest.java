package com.second_hand_trading_platform.second_hand_trading_platform.modules.common.dto.input.authed_query;

import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.goods.Goods;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.goods.ImageModel;
import lombok.Data;

import java.util.List;

@Data
public class AddNewGoodsRequest extends AuthQuery {
    Goods goods;
    List<ImageModel> imgs;
}
