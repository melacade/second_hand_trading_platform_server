package com.second_hand_trading_platform.second_hand_trading_platform.service;

import com.second_hand_trading_platform.second_hand_trading_platform.mybatis.GoodsDAO;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.goods.*;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.user.User;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.user.UserBaseInfo;
import org.apache.tomcat.util.buf.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GoodsService {
    @Autowired
    GoodsDAO goodsDAOMapper;
    @Autowired
    UserService userService;

    public Integer addNewGoods(Goods goods) {
        Integer maxId = goodsDAOMapper.getMaxId();
        int id = maxId == null ? 1 : maxId + 1;
        goods.setId(id);
        goodsDAOMapper.addNewGoods(goods);
        return id;
    }

    public boolean addGoodsImages(List<ImageModel> imgs) {
        boolean flag = true;
        for (ImageModel img : imgs) {
            Integer imageModel = goodsDAOMapper.addNewGoodsImage(img);
            if (imageModel == null) {
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
        int index = (Integer.parseInt(page) - 1) * count;
        return goodsDAOMapper.search(text, index, count, orderByTime, orderByPrice);
    }

    public Goods getGoodsInfo(int id) {
        return goodsDAOMapper.getGoodsById(id);
    }

    public List<ImageModel> getGoodsImages(int id) {
        return goodsDAOMapper.getGoodsImagesByGoodsId(id);
    }


    public List<Goods> getGoodsByPage(int page, int count) {
        int start = (page - 1) * count;
        return goodsDAOMapper.getGoodsByPage(count, start);
    }

    public String createOrder(Order order) {
        String id = UUID.randomUUID().toString().replaceAll("-", "");
        Integer goodsId = order.getGoodsId();
        Goods goods = goodsDAOMapper.getGoodsById(goodsId);
        if (goods.getCount() < order.getCount()) {
            return null;
        }
        order.setId(id);
        order.setBaseUserId(userService.getCurrentUser().getUserID());
        int count = goodsDAOMapper.createOrder(order);
        if (count == 1) {
            return id;
        }
        return null;
    }

    public boolean payOrder(String orderId) {

        Order order = goodsDAOMapper.getOrderById(orderId, userService.getCurrentUser().getUserID());
        if (order == null || order.getStatus() != 0) {
            return false;
        }
        User currentUser = userService.getCurrentUser();
        String uid = currentUser.getUserBaseInfo().getId();
        Integer goodsId = order.getGoodsId();
        // 同步锁防止支付错误
        synchronized (String.valueOf(goodsId)) {
            Goods goods = goodsDAOMapper.getGoodsById(goodsId);
            if (goods == null || goods.getCount() < order.getCount()) {
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

    public boolean cancelOrder(String orderId) {
        Order order = goodsDAOMapper.getOrderById(orderId, userService.getCurrentUser().getUserID());
        if (order == null || order.getStatus() == -1) {
            return false;
        }
        User currentUser = userService.getCurrentUser();
        String uid = currentUser.getUserBaseInfo().getId();
        order.setBaseUserId(uid);
        order.setStatus(-1);
        int count = goodsDAOMapper.updateOrder(order);
        return count == 1;
    }

    public List<Order> getOrderByPage(int page, int count) {
        int start = (page - 1) * count;
        return goodsDAOMapper.getOrdersByPage(start, count, userService.getCurrentUser().getUserID());
    }

    public List<Order> getPayingOrderByPage(int page, int count) {
        int start = (page - 1) * count;
        return goodsDAOMapper.getPayingOrdersByPage(start, count, userService.getCurrentUser().getUserID());
    }

    public List<Order> getReceivingOrderByPage(int page, int count) {
        int start = (page - 1) * count;
        return goodsDAOMapper.getReceivingOrdersByPage(start, count, userService.getCurrentUser().getUserID());
    }

    public List<Order> getReturnedOrderByPage(int page, int count) {
        int start = (page - 1) * count;
        return goodsDAOMapper.getReturnedOrdersByPage(start, count, userService.getCurrentUser().getUserID());
    }

    public Order getOrderInfoById(String orderId) {
        return goodsDAOMapper.getOrderById(orderId, userService.getCurrentUser().getUserID());
    }

    public String getGoodsDefaultImage(int goodsId) {
        Goods goodsInfo = this.getGoodsInfo(goodsId);
        return goodsInfo.getDefaultImage();
    }


    public GoodsLabelInfo addLabel(GoodsLabelInfo labelInfo) {
        GoodsLabelInfo info = goodsDAOMapper.getLabelByName(labelInfo.getName());
        if (info != null) {
            return info;
        }
        Integer max = goodsDAOMapper.getLabelMaxId();
        int id = max == null ? 1 : max + 1;
        labelInfo.setId(id);
        int count = goodsDAOMapper.addLabel(labelInfo);
        return count == 1 ? labelInfo : null;
    }

    public List<GoodsLabelInfo> getLabels() {
        return goodsDAOMapper.getLabels();
    }

    public boolean addAttachLabels(List<Integer> labels, Integer goodsId) {
        for (Integer label : labels) {
            int count = goodsDAOMapper.addAttachLabel(label, goodsId);
            if (count == 0) {
                return false;
            }
        }
        return true;
    }

    public List<Goods> getSaleGoodsByPage(int page, int count) {
        int start = (page - 1) * count;
        String userID = userService.getCurrentUser().getUserID();
        List<Goods> saleGoodsList = goodsDAOMapper.getSaleGoods(userID, start, count);
        for (Goods saleGoods : saleGoodsList) {
            Integer c = goodsDAOMapper.getSailedCountByGoodsId(saleGoods.getId());
            int num = 0;
            if (c != null) {
                num = c;
            }
            saleGoods.setSales(num);
        }
        return saleGoodsList;
    }

    public List<Map<String, Object>> getCommentsByPage(Integer goodsId, Integer page, Integer count) {
        int start = (page - 1) * count;
        List<Comments> comments = goodsDAOMapper.getCommentsByPage(goodsId, start, count);
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, UserBaseInfo> cache = new HashMap<>();
        for (Comments comment : comments) {
            UserBaseInfo info = cache.getOrDefault(comment.getUserBaseId(), null);
            if (info == null) {
                info = userService.getUserById(comment.getUserBaseId());
                cache.put(comment.getUserBaseId(), info);
            }
            Map<String, Object> temp = new HashMap<>();
            temp.put("comment", comment);
            temp.put("user", info);
            result.add(temp);
        }
        return result;
    }

    public boolean addComment(String goodsId, String content) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null ){
            return false;
        }
        return goodsDAOMapper.addComment(currentUser.getUserID(),goodsId,content) == 1;
    }

    public List<Goods> getRecommend() {
        String uid = null;
        try{
            User currentUser = userService.getCurrentUser();
            uid = currentUser.getUserID();
        }catch (Exception e){
            uid = null;
        }

        if (uid == null){
            return this.getHotGoods();
        }else{
            return this.getRecommendByCollected(uid);
        }
    }

    private List<Goods> getRecommendByCollected(String uid) {
        List<String> collected = goodsDAOMapper.getCollectedGoodsId(uid);
        if(collected.size() == 0){
            return this.getHotGoods();
        }
        String join = StringUtils.join(collected, ',');
        List<Recommend> collectedLabels = goodsDAOMapper.getCollectedLabels(join);
        StringBuilder sb = new StringBuilder();
        if(collectedLabels.size() == 0) {
            return this.getHotGoods();
        }
        for (int i = 0; i < collectedLabels.size(); i++) {
            sb.append(collectedLabels.get(i).getId());
            if(i != collectedLabels.size()-1){
                sb.append(",");
            }
        }
        List<Goods> result = goodsDAOMapper.getRecommend(sb.toString(),join);
        if(result.size() < 10){
            List<Goods> recommend = this.getHotGoods();
            int index = 0;
            Set<Integer> goodsIds = new HashSet<>();
            for (Goods goods : result) {
                goodsIds.add(goods.getId());
            }
            while (result.size()<10&&index<recommend.size()){
                if(!goodsIds.contains(recommend.get(index).getId())){
                    result.add(recommend.get(index));
                }
                index++;
            }
        }
        return result;
    }

    private List<Goods> getHotGoods() {
        List<Goods> hotGoods = goodsDAOMapper.getHotGoods();
        if(hotGoods.size()<5){
            List<Goods> goodsByPage = this.getGoodsByPage(1, 10);
            int index = 0;
            Set<Integer> goodsIds = new HashSet<>();
            for (Goods goods : hotGoods) {
                goodsIds.add(goods.getId());
            }
            while(hotGoods.size()<5&&index<goodsByPage.size()){
                if(!goodsIds.contains(goodsByPage.get(index).getId())){
                    hotGoods.add(goodsByPage.get(index));
                }
                index++;
            }
        }
        return hotGoods;
    }
}
