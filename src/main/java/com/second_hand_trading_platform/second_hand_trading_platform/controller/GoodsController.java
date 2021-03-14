package com.second_hand_trading_platform.second_hand_trading_platform.controller;

import com.second_hand_trading_platform.second_hand_trading_platform.modules.common.dto.input.authed_query.AddNewGoodsRequest;
import com.second_hand_trading_platform.second_hand_trading_platform.modules.common.dto.output.ApiResult;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.goods.Goods;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.goods.ImageModel;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.user.User;
import com.second_hand_trading_platform.second_hand_trading_platform.service.GoodsService;
import com.second_hand_trading_platform.second_hand_trading_platform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/goods")
public class GoodsController {
    @Autowired
    GoodsService goodsService;
    @Autowired
    UserService userService;


    @PostMapping("/addNewGoods")
    ApiResult addNewGoods(@RequestBody AddNewGoodsRequest req){
        Goods goods = req.getGoods();
        if(goods.getCount() == 0){
            goods.setCount(1);
        }
        User currentUser = userService.getCurrentUser();
        List<ImageModel> imgs = req.getImgs();
        goods.setDefaultImage(imgs.get(0).getImage());
        goods.setOwnerId(currentUser.getUserBaseInfo().getId());
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

    @PostMapping("/info")
    ApiResult goodsInfo(@RequestBody String id){
        Goods goods = goodsService.getGoodsInfo(id);
        if(goods == null){
            return ApiResult.fail("没有相关商品信息");
        }
        List<ImageModel> goodsImages = goodsService.getGoodsImages(id);
        Map<String,Object> map = new HashMap<>();
        map.put("goodsInfo",goods);
        map.put("images", goodsImages);
        return ApiResult.ok("查询成功",map);
    }
}
