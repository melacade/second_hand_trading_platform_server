package com.second_hand_trading_platform.second_hand_trading_platform.controller;

import com.second_hand_trading_platform.second_hand_trading_platform.modules.common.dto.input.authed_query.AddNewGoodsRequest;
import com.second_hand_trading_platform.second_hand_trading_platform.modules.common.dto.output.ApiResult;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.goods.Goods;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.goods.GoodsLabelInfo;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.goods.ImageModel;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.goods.Order;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.user.User;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.user.UserAddress;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.user.UserBaseInfo;
import com.second_hand_trading_platform.second_hand_trading_platform.service.GoodsService;
import com.second_hand_trading_platform.second_hand_trading_platform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    ApiResult addNewGoods(@RequestBody AddNewGoodsRequest req) {
        Goods goods = req.getGoods();
        if (goods.getCount() == 0) {
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
        boolean t = goodsService.addAttachLabels(goods.getLabels(),newGoods);
        if (newGoods == null) {
            return ApiResult.fail("添加失败");
        }
        return ApiResult.ok("添加成功", newGoods);
    }

    @PostMapping("/search")
    ApiResult search(@RequestBody Map<String, String> body) {
        List<Goods> goods = goodsService.search(body);

        return ApiResult.ok("查询成功", goods);
    }

    @PostMapping("/info")
    ApiResult goodsInfo(@RequestBody int id) {
        Goods goods = goodsService.getGoodsInfo(id);
        if (goods == null) {
            return ApiResult.fail("没有相关商品信息");
        }
        List<ImageModel> goodsImages = goodsService.getGoodsImages(id);
        Map<String, Object> map = new HashMap<>();
        map.put("goodsInfo", goods);
        map.put("images", goodsImages);
        UserBaseInfo owner = userService.getUserById(goods.getOwnerId());
        owner.setToken(null);
        map.put("owner", owner);
        return ApiResult.ok("查询成功", map);
    }


    @GetMapping("/getGoodsByPage/{page}/{count}")
    ApiResult getGoodsByPage(@PathVariable("page") Integer page, @PathVariable("count") Integer count) {
        List<Goods> goodsList = goodsService.getGoodsByPage(page, count);
        return ApiResult.ok("查询成功", goodsList);
    }

    @PostMapping("/createOrder")
    ApiResult createOrder(@RequestBody Order order) {
        String id = goodsService.createOrder(order);
        if (id != null) {
            return ApiResult.ok("订单创建成功", id);

        }
        return ApiResult.fail("订单创建失败");
    }

    @GetMapping("/getOrderByPage/{page}/{count}")
    ApiResult getOrderByPage(@PathVariable("page") Integer page, @PathVariable("count") Integer count){
        List<Order> orderByPage = goodsService.getOrderByPage(page, count);
        for (Order order : orderByPage) {
            Goods goods = goodsService.getGoodsInfo(order.getGoodsId());
            order.setImage(goods.getDefaultImage());
            order.setName(goods.getName());
        }
        return ApiResult.ok("订单查询成功",orderByPage);
    }
    @GetMapping("/getReturnOrderByPage/{page}/{count}")
    ApiResult getReturnOrderByPage(@PathVariable("page") Integer page, @PathVariable("count") Integer count){
        List<Order> orderByPage = goodsService.getReturnedOrderByPage(page, count);
        for (Order order : orderByPage) {
            Goods goods = goodsService.getGoodsInfo(order.getGoodsId());
            order.setImage(goods.getDefaultImage());
            order.setName(goods.getName());
        }
        return ApiResult.ok("订单查询成功",orderByPage);
    }
    @GetMapping("/getPayingOrderByPage/{page}/{count}")
    ApiResult getPayingOrderByPage(@PathVariable("page") Integer page, @PathVariable("count") Integer count){
        List<Order> orderByPage = goodsService.getPayingOrderByPage(page, count);
        for (Order order : orderByPage) {
            Goods goods = goodsService.getGoodsInfo(order.getGoodsId());
            order.setImage(goods.getDefaultImage());
            order.setName(goods.getName());
        }
        return ApiResult.ok("订单查询成功",orderByPage);
    }
    @GetMapping("/getReceivingOrderByPage/{page}/{count}")
    ApiResult getReceivingOrderByPage(@PathVariable("page") Integer page, @PathVariable("count") Integer count){
        List<Order> orderByPage = goodsService.getReceivingOrderByPage(page, count);
        for (Order order : orderByPage) {
            Goods goods = goodsService.getGoodsInfo(order.getGoodsId());
            order.setImage(goods.getDefaultImage());
            order.setName(goods.getName());
        }
        return ApiResult.ok("订单查询成功",orderByPage);
    }

    @PostMapping("/payOrder")
    ApiResult payOrder(@RequestBody String orderId) {

        if (goodsService.payOrder(orderId)) {
            return ApiResult.ok("支付成功", true);
        }
        return ApiResult.fail("支付失败");

    }

    @GetMapping("/orderInfo/{orderId}")
    ApiResult getGoodsInfo(@PathVariable("orderId") String orderId) {
        Order order = goodsService.getOrderInfoById(orderId);
        if (order == null) {
            return ApiResult.fail("查询失败");
        }
        Goods goods = goodsService.getGoodsInfo(order.getGoodsId());
        UserBaseInfo user = userService.getUserById(goods.getOwnerId());
        UserAddress addr = userService.getAddressById(order.getAddress());
        user.setToken(null);
        Map<String, Object> data = new HashMap<>();
        data.put("order", order);
        data.put("owner", user);
        data.put("goodsInfo", goods);
        data.put("address",addr);
        return ApiResult.ok("查询订单成功", data);
    }


    @PostMapping("/addLabel")
    ApiResult addLabel(@RequestBody GoodsLabelInfo labelInfo){
        if(labelInfo.getName().length() >= 2){
            GoodsLabelInfo goodsLabelInfo = goodsService.addLabel(labelInfo);
            if(goodsLabelInfo != null){
                return ApiResult.ok("标签创建成功",goodsLabelInfo);
            }
        }
        return ApiResult.fail("标签创建失败！");
    }

    @GetMapping("/getLabels")
    ApiResult getLabels(){
        List<GoodsLabelInfo> labels =  goodsService.getLabels();
        return ApiResult.ok("标签查询成功！",labels);
    }


    @GetMapping("/getSaleGoodsByPage/{page}/{count}")
    ApiResult getSaleGoodsByPage(@PathVariable("page") Integer page, @PathVariable("count") Integer count){
        List<Goods> goodsList = goodsService.getSaleGoodsByPage(page,count);
        return ApiResult.ok("出售商品查询成功！",goodsList);
    }

    @GetMapping("/getCommentsByPage/{goodsId}/{page}/{count}")
    ApiResult getCommentsByPage(@PathVariable Integer goodsId, @PathVariable Integer page, @PathVariable Integer count){
        List<Map<String,Object>> comments = goodsService.getCommentsByPage(goodsId,page,count);
        return ApiResult.ok("评论查询成功",comments);
    }

    @PostMapping("/postComment")
    ApiResult postComment(@RequestBody Map<String,String> comment){

        return goodsService.addComment(comment.get("goodsId"),comment.get("content")) ? ApiResult.ok("评论创建成功") : ApiResult.fail("评论创建失败");
    }

    @GetMapping("/getRecommend")
    ApiResult getRecommend(){
        return ApiResult.ok("推荐成功",goodsService.getRecommend());
    }

}
