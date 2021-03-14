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
    @Insert("INSERT INTO goods(_id,name,info,price,original_price,default_image,count,new_percentage,owner_id) VALUES(#{id},#{name},#{info},#{price},#{originalPrice},#{defaultImage},#{count},#{newPercentage},#{ownerId})")
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

    @Select("SELECT * FROM goods WHERE _id=#{id}")
    Goods getGoodsById(@Param("id") String id);


    @Select("SELECT * FROM goods_image WHERE goods_id=#{id} ORDER BY _id")
    List<ImageModel> getGoodsImagesByGoodsId(@Param("id") String id);

    @Select("SELECT MAX(_id) FROM goods")
    Integer getMaxId();

    //List<Goods> getGoodsByUserId();
}
