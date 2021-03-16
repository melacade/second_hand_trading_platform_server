package com.second_hand_trading_platform.second_hand_trading_platform.service;

import com.second_hand_trading_platform.second_hand_trading_platform.mybatis.GoodsDAO;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.goods.Goods;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.goods.ImageModel;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.goods.Order;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class GoodsService {
    @Autowired
    GoodsDAO goodsDAOMapper;
    @Autowired
    UserService userService;

    public Integer addNewGoods(Goods goods){
        int id = goodsDAOMapper.getMaxId()+1;
        goods.setId(id);
        goodsDAOMapper.addNewGoods(goods);
        return id;
    }

    public boolean addGoodsImages(List<ImageModel> imgs){
        boolean flag = true;
        for (ImageModel img : imgs) {
            Integer imageModel = goodsDAOMapper.addNewGoodsImage(img);
            if(imageModel == null){
                flag = false;
                break;
            }

        }
        return flag;
    }

    public List<Goods> search(Map<String, String> body) {
        String text = body.get("text");
        String orderByTime = body.get("time");
        String orderByPrice = body.get("price");
        String page = body.get("page");
        int count = 10;
        int index = (Integer.parseInt(page) - 1)*count;
       return goodsDAOMapper.search(text,index,count,orderByTime,orderByPrice);
    }

    public Goods getGoodsInfo(int id) {
        return goodsDAOMapper.getGoodsById(id);
    }

    public List<ImageModel> getGoodsImages(int id) {
        return goodsDAOMapper.getGoodsImagesByGoodsId(id);
    }


    public List<Goods> getGoodsByPage(int page, int count) {
        int start = (page - 1) * count;
        return goodsDAOMapper.getGoodsByPage(count,start);
    }

    public String createOrder(Order order) {
        String id = UUID.randomUUID().toString().replaceAll("-", "");
        order.setId(id);
        order.setBaseUserId(userService.getCurrentUser().getUserID());
        int count = goodsDAOMapper.createOrder(order);
        if(count == 1){
            return id;
        }
        return null;
    }

    public boolean payOrder(String orderId)  {

        Order order = goodsDAOMapper.getOrderById(orderId, userService.getCurrentUser().getUserID());
        if(order == null||order.getStatus() != 0){
            return false;
        }
        User currentUser = userService.getCurrentUser();
        String uid = currentUser.getUserBaseInfo().getId();
        Integer goodsId = order.getGoodsId();
        // 同步锁防止支付错误
        synchronized(String.valueOf(goodsId)){
            Goods goods = goodsDAOMapper.getGoodsById(goodsId);
            if( goods == null||goods.getCount() < order.getCount()){
                return false;
            }
            goods.setCount(goods.getCount() - order.getCount());
            goodsDAOMapper.updateGoods(goods);
        }

        order.setBaseUserId(uid);
        order.setStatus(1);
        int count = goodsDAOMapper.updateOrder(order);
        return count == 1;
    }

    public boolean cancelOrder(String orderId){
        Order order = goodsDAOMapper.getOrderById(orderId, userService.getCurrentUser().getUserID());
        if(order == null||order.getStatus() == -1){
            return false;
        }
        User currentUser = userService.getCurrentUser();
        String uid = currentUser.getUserBaseInfo().getId();
        order.setBaseUserId(uid);
        order.setStatus(-1);
        int count = goodsDAOMapper.updateOrder(order);
        return count == 1;
    }

    public List<Order> getOrderByPage(int page, int count){
        int start = (page - 1) * count;
        return goodsDAOMapper.getOrdersByPage(start, count, userService.getCurrentUser().getUserID());
    }

    public Order getOrderInfoById(String orderId) {
        return goodsDAOMapper.getOrderById(orderId,userService.getCurrentUser().getUserID());
    }
}
