package com.second_hand_trading_platform.second_hand_trading_platform.mybatis;

import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.goods.Goods;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.goods.ImageModel;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface GoodsDAO {
    @Insert("INSERT INTO goods(name,info,price,shop_id,original_price,default_image,count,new_percentage) VALUES(#{name},#{info},#{price},#{shopId},#{originalPrice},#{defaultImage},#{count},#{newPercentage})")
    Integer addNewGoods(Goods goods);
    @Select("Select * FROM goods order by _id LIMIT #{start},#{count}")
    List<Goods> getGoodsByPage(int count,int start);
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

    //List<Goods> getGoodsByUserId();
}
