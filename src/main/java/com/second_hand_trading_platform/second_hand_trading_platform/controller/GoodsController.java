package com.second_hand_trading_platform.second_hand_trading_platform.controller;

import com.second_hand_trading_platform.second_hand_trading_platform.modules.common.dto.input.authed_query.AddNewGoodsRequest;
import com.second_hand_trading_platform.second_hand_trading_platform.modules.common.dto.output.ApiResult;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.goods.Goods;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.goods.ImageModel;
import com.second_hand_trading_platform.second_hand_trading_platform.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/goods")
public class GoodsController {
    @Autowired
    GoodsService goodsService;

    @PostMapping("/addNewGoods")
    ApiResult addNewGoods(@RequestBody AddNewGoodsRequest req){
        Goods goods = req.getGoods();
        if(goods.getCount() == 0){
            goods.setCount(1);
        }
        List<ImageModel> imgs = req.getImgs();
        goods.setDefaultImage(imgs.get(0).getImage());
        Integer newGoods = goodsService.addNewGoods(goods);
        for (ImageModel img : imgs) {
            img.setGoodsId(newGoods);
        }
        boolean b = goodsService.addGoodsImages(imgs);
        if(newGoods == null){
            return ApiResult.fail("添加失败");
        }
        return ApiResult.ok("添加成功",newGoods);
    }

    @PostMapping("/search")
    ApiResult search(@RequestBody Map<String,String> body){
        List<Goods> goods = goodsService.search(body);

        return ApiResult.ok("查询成功",goods);
    }
}
