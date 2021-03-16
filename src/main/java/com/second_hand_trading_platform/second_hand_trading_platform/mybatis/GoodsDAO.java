package com.second_hand_trading_platform.second_hand_trading_platform.mybatis;

import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.goods.Goods;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.goods.ImageModel;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.goods.Order;
import org.apache.ibatis.annotations.*;
import org.springframework.security.core.parameters.P;

import java.util.List;

@Mapper
public interface GoodsDAO {
    @Insert("INSERT INTO goods(_id,name,info,price,original_price,default_image,count,new_percentage,owner_id) VALUES(#{id},#{name},#{info},#{price},#{originalPrice},#{defaultImage},#{count},#{newPercentage},#{ownerId})")
    Integer addNewGoods(Goods goods);

    @Select("Select * FROM goods order by created_at LIMIT #{start},#{count}")
    List<Goods> getGoodsByPage(@Param("count") int count, @Param("start") int start);

    @Insert("INSERT INTO goods_image(goods_id,image) VALUES(#{goodsId},#{image})")
    Integer addNewGoodsImage(ImageModel img);

    @Select("<script> " +
            "SELECT * FROM goods WHERE name like concat('%',#{text},'%')" +
            "<if test='time!=null and price==null'> order by created_at desc</if>" +
            "<if test='time==null and price!=null'> order by price desc</if>" +
            "<if test='time==null and price==null'> order by created_at</if>" +
            "<if test='time!=null and price!=null'> order by price desc, created at desc</if>" +
            " LIMIT #{index},#{count}"+
            "</script>")
    List<Goods> search(@Param("text") String text,@Param("index") int index,@Param("count") int count,@Param("time") String orderByTime,@Param("price") String orderByPrice);

    @Select("SELECT * FROM goods WHERE _id=#{id}")
    Goods getGoodsById(@Param("id") int id);


    @Select("SELECT * FROM goods_image WHERE goods_id=#{id} ORDER BY _id")
    List<ImageModel> getGoodsImagesByGoodsId(@Param("id") int id);

    @Select("SELECT MAX(_id) FROM goods")
    Integer getMaxId();

    @Insert("INSERT INTO `order`(_id,goods_id,price,base_user_id,address,count) VALUES(#{id},#{goodsId},#{price},#{baseUserId},#{address},#{count})")
    int createOrder(Order order);

    @Update("UPDATE `order` SET status=#{status},price=#{price},address=#{address},count=#{count} WHERE _id=#{id}")
    int updateOrder(Order order);

    @Select("Select * FROM `order` WHERE user_base_id=#{uid} order by created_at LIMIT #{start},#{count}")
    List<Order> getOrdersByPage(@Param("start") int start, @Param("count") int count, @Param("uid") String userId);

    @Update("UPDATE goods SET name=#{name},info=#{info},price=#{price},original_price=#{originalPrice},default_image=#{defaultImage},`count`=#{count},new_percentage=#{newPercentage} WHERE _id=#{id}")
    void updateGoods(Goods goods);

    @Select("SELECT * FROM `order` WHERE _id=#{orderId} and base_user_id=#{userId}")
    Order getOrderById(@Param("orderId") String orderId,@Param("userId") String userId);


    //List<Goods> getGoodsByUserId();
}
