package com.second_hand_trading_platform.second_hand_trading_platform.service;

import com.second_hand_trading_platform.second_hand_trading_platform.mybatis.GoodsDAO;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.goods.Goods;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.goods.ImageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class GoodsService {
    @Autowired
    GoodsDAO goodsDAOMapper;

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

    public Goods getGoodsInfo(String id) {
        return goodsDAOMapper.getGoodsById(id);
    }

    public List<ImageModel> getGoodsImages(String id) {
        return goodsDAOMapper.getGoodsImagesByGoodsId(id);
    }
}
