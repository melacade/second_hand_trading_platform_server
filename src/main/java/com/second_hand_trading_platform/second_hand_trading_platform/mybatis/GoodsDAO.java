package com.second_hand_trading_platform.second_hand_trading_platform.mybatis;

import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.goods.*;
import org.apache.ibatis.annotations.*;
import org.springframework.security.core.parameters.P;

import java.util.List;

@Mapper
public interface GoodsDAO {
    @Insert("INSERT INTO goods(_id,name,info,price,original_price,default_image,count,new_percentage,owner_id) VALUES(#{id},#{name},#{info},#{price},#{originalPrice},#{defaultImage},#{count},#{newPercentage},#{ownerId})")
    Integer addNewGoods(Goods goods);

    @Select("Select * FROM goods order by created_at DESC LIMIT #{start},#{count}")
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

    @Select("Select * FROM `order` WHERE base_user_id=#{uid} and status=0 order by created_at LIMIT #{start},#{count}")
    List<Order> getPayingOrdersByPage(@Param("start") int start, @Param("count") int count, @Param("uid") String userId);


    @Select("Select * FROM `order` WHERE base_user_id=#{uid} and status=1 order by created_at LIMIT #{start},#{count}")
    List<Order> getReceivingOrdersByPage(@Param("start") int start, @Param("count") int count, @Param("uid") String userId);

    @Select("Select * FROM `order` WHERE base_user_id=#{uid} and status=-1 order by created_at LIMIT #{start},#{count}")
    List<Order> getReturnedOrdersByPage(@Param("start") int start, @Param("count") int count, @Param("uid") String userId);

    @Select("Select * FROM `order` WHERE base_user_id=#{uid} order by created_at LIMIT #{start},#{count}")
    List<Order> getOrdersByPage(@Param("start") int start, @Param("count") int count, @Param("uid") String userId);



    @Update("UPDATE goods SET name=#{name},info=#{info},price=#{price},original_price=#{originalPrice},default_image=#{defaultImage},`count`=#{count},new_percentage=#{newPercentage} WHERE _id=#{id}")
    void updateGoods(Goods goods);

    @Select("SELECT * FROM `order` WHERE _id=#{orderId} and base_user_id=#{userId}")
    Order getOrderById(@Param("orderId") String orderId,@Param("userId") String userId);

    @Select("SELECT MAX(_id) FROM goods_label_info")
    Integer getLabelMaxId();

    @Insert("INSERT INTO goods_label_info(name,info) VALUES(#{name},#{info})")
    int addLabel(GoodsLabelInfo labelInfo);

    @Select("SELECT * FROM goods_label_info")
    List<GoodsLabelInfo> getLabels();

    @Select("SELECT * FROM goods_label_info WHERE name=#{name}")
    GoodsLabelInfo getLabelByName(@Param("name") String name);


    @Insert("INSERT INTO goods_attach_label(goods_id,goods_label_info) VALUES(#{goodsId},#{label})")
    int addAttachLabel(@Param("label") Integer label,@Param("goodsId") Integer goodsId);


    @Select("SELECT * FROM goods WHERE owner_id=#{userID} order by created_at DESC LIMIT #{start},#{count}")
    List<Goods> getSaleGoods(@Param("userID") String userID,@Param("start") int start,@Param("count") int count);

    @Select("SELECT SUM(count) FROM `order` WHERE goods_id=#{id} and status>0")
    Integer getSailedCountByGoodsId(Integer id);

    @Select("SELECT * FROM `comments` WHERE goods_id=#{goodsId} ORDER BY created_at DESC LIMIT #{start},#{count}")
    List<Comments> getCommentsByPage(@Param("goodsId") Integer goodsId,@Param("start") int start,@Param("count") Integer count);

    @Insert("INSERT INTO `comments`(user_base_id,goods_id,content) VALUES(#{userId},#{goodsId},#{content})")
    Integer addComment(@Param("userId") String userID,@Param("goodsId") String goodsId,@Param("content") String content);

    @Select("SELECT *\n" +
            "FROM goods g\n" +
            "WHERE g._id\n" +
            "          IN\n" +
            "      (SELECT *\n" +
            "       FROM (\n" +
            "                SELECT t.goods_id\n" +
            "                FROM (SELECT o.goods_id        AS goods_id,\n" +
            "                             COUNT(o.goods_id) AS con\n" +
            "                      FROM `order` o\n" +
            "                      GROUP BY o.goods_id) t\n" +
            "                ORDER BY t.con\n" +
            "                LIMIT 0,10\n" +
            "            ) m);")
    List<Goods> getHotGoods();

    @Select("SELECT goods_id FROM collect WHERE user_base_id=#{uid}")
    List<String> getCollectedGoodsId(@Param("uid") String uid);

    @Select("SELECT goods_label_info AS id, COUNT(goods_label_info) AS con\n" +
            "FROM goods_attach_label\n" +
            "WHERE goods_id IN (${join})\n" +
            "GROUP BY goods_label_info\n" +
            "ORDER BY con DESC\n" +
            "limit 0,3")
    List<Recommend> getCollectedLabels(@Param("join") String join);

    @Select("SELECT * FROM goods WHERE _id IN (SELECT b.goods_id FROM (SELECT * FROM goods_attach_label WHERE goods_id NOT IN (${collected}) AND goods_label_info IN (${labels}))b) LIMIT 0,10")
    List<Goods> getRecommend(@Param("labels") String labels,@Param("collected") String goods);

    //
    //List<Goods> getGoodsByUserId();
}
