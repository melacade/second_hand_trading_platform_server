package com.second_hand_trading_platform.second_hand_trading_platform.mybatis;


import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.user.UserAddress;
import org.apache.ibatis.annotations.*;

import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.user.*;

import java.util.List;
@Mapper
public interface UserDAO {
    @Select("SELECT * FROM user_private WHERE account = #{username}")
    UserPrivate lodUserPrivateByUsername(@Param("username") String username);

    @Select("SELECT * FROM user_base_info WHERE _id = #{_id}")
    UserBaseInfo loadUserBaseInfoByUserID(@Param("_id") String id);

    @Select("SELECT * FROM role WHERE user_base_id = #{_id}")
    List<Role> loadRolesByUserID(@Param("_id") String id);

    @Select("SELECT * FROM base_role WHERE _id = #{_id}")
    BaseRole loadRoleByRoleID(@Param("_id") String id);

    @Select("SELECT * FROM user_private WHERE _id = #{_id}")
    UserPrivate selectById(@Param("_id") Integer id);

    @Update("UPDATE user_private SET level=#{level}, password=#{password},locked=#{locked},salt=#{salt} where _id=#{id}")
    void updateUserPrivate(UserPrivate user);

    @Update("UPDATE user_base_info SET name=#{name},token=#{token},auth_time=#{authTime},phone=#{phone},avator=#{avator} where _id=#{id}")
    void updateUserBaseInfo(UserBaseInfo user);

    @Select("SELECT * FROM user_base_info WHERE token = #{token}")
    UserBaseInfo loadUserBaseInfoByUserToken(@Param("token") String token);

    @Select("SELECT * FROM user_private WHERE user_id = #{id}")
    UserPrivate loadUserPrivateByUserID(@Param("id") String id);

    @Insert("INSERT INTO user_base_info(_id,name,phone,avator,token,auth_time) values (#{id},#{name},#{phone},#{avator},#{token},#{authTime})")
    void createBaseUser(UserBaseInfo userBaseInfo);

    @Insert("INSERT INTO user_private(user_id,level,password,account,locked,salt) values (#{userID},#{level},#{password},#{account},#{locked},#{salt})")
    void createPrivateUser(UserPrivate userPrivate);

    @Insert("INSERT INTO role(role_id,user_base_id) VALUES (#{role_id},#{userID})")
    void createRole(@Param("role_id") int role_id,@Param("userID") String userID);

    @Select("SELECT count(_id) FROM user_private WHERE account=#{account}")
    int accountExisted(@Param("account") String account);

    @Insert("INSERT INTO security_questions(question,answer,user_id) VALUES(#{question},#{answer},#{userID})")
    void createSecurityQuestion(SecurityQuestion question);

    @Select("SELECT * FROM security_questions where question=#{question} and user_id=#{userID})")
    SecurityQuestion findQuestion(String question, String userID);

    @Select("SELECT count(_id) FROM security_questions WHERE user_id=#{user_id}")
    int hasSecurityProblem(@Param("user_id")String userId);

    @Select("SELECT _id,question FROM security_questions WHERE user_id=#{user_id}")
    List<SecurityQuestion> checkSecurity(@Param("user_id") String userId);

    @Select("SELECT * FROM security_questions WHERE user_id=#{user_id} order by _id")
    List<SecurityQuestion> getSecurityProblemsByUserID(@Param("user_id") String userID);

    @Update("UPDATE security_questions set question=#{question}, answer=#{answer}, change_time=#{changeTime} where _id=#{id}")
    void updateSecurityQuestion(SecurityQuestion validate);


    @Update("UPDATE user_private set payment_PWD=#{payment} where user_id=#{user_id}")
    void updatePayment(@Param("payment") String payment,@Param("user_id") String userID);

    @Select("SELECT * FROM user_address WHERE user_base_id=#{userId} ORDER BY is_default DESC")
    List<UserAddress> getAddressByUserId(@Param("userId") String userId);

    @Update("UPDATE user_address SET country=#{country},province=#{province},city=#{city},detail=#{detail},is_default=#{isDefault} WHERE _id=#{id}")
    Integer updateAddress(UserAddress address);

    @Select("SELECT * FROM user_address WHERE user_base_id=#{userId} and is_default=true")
    List<UserAddress> getDefaultAddress(@Param("userId") String userId);

    @Select("SELECT MAX(_id) FROM user_address")
    Integer getAddressMaxId();

    @Insert("INSERT INTO user_address(country,province,city,detail,user_base_id,is_default) VALUES(#{country},#{province},#{city},#{detail},#{userBaseId},#{isDefault})")
    void addUserAddress(UserAddress userAddress);

    @Select("SELECT * FROM user_address WHERE _id=#{address}")
    UserAddress getAddressById(@Param("address") Integer address);

    @Select("SELECT ACCOUNT(_id) FROM user_address WHERE user_base_id=#{userId}")
    int getUserAddressCount(@Param("userId") String userId);
}
