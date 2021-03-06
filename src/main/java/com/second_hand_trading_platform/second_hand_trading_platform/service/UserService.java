package com.second_hand_trading_platform.second_hand_trading_platform.service;

import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.user.UserAddress;
import com.second_hand_trading_platform.second_hand_trading_platform.modules.common.dto.input.authed_query.AddPaymentQuery;
import com.second_hand_trading_platform.second_hand_trading_platform.modules.common.dto.input.authed_query.ChangePWDQuery;
import com.second_hand_trading_platform.second_hand_trading_platform.modules.common.dto.input.authed_query.ResetPayment;
import com.second_hand_trading_platform.second_hand_trading_platform.modules.common.dto.input.nomal_query.RegisterQuery;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.user.*;
import com.second_hand_trading_platform.second_hand_trading_platform.utils.PasswordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;
import com.second_hand_trading_platform.second_hand_trading_platform.mybatis.UserDAO;

import java.util.*;

/**
 *
 */
@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserDAO userMapperDAO;



    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //从数据库中查找user
        UserPrivate userPrivate = userMapperDAO.lodUserPrivateByUsername(username);
        // 如果不存在抛出异常
        if(userPrivate == null){
            throw new UsernameNotFoundException("not find:" + username);
        }
        // 配置user
        UserBaseInfo userBaseInfo = userMapperDAO.loadUserBaseInfoByUserID(userPrivate.getUserID());
        List<Role> roles = userMapperDAO.loadRolesByUserID(userBaseInfo.getId());
        for(Role role : roles){
            role.setBaseRole(userMapperDAO.loadRoleByRoleID(role.getRoleID()));
        }
        User user = new User();
        user.setUserPrivate(userPrivate);
        user.setUserRoles(roles);
        user.setUserBaseInfo(userBaseInfo);
        user.setUserID(userBaseInfo.getId());
        return user;
    }

    public User getUserByToken(String token) {
        //从数据库中查找user
        UserBaseInfo userBaseInfo = userMapperDAO.loadUserBaseInfoByUserToken(token);
        // 如果不存在抛出异常
        if(userBaseInfo == null){
            return null;
        }
        // 配置user
        UserPrivate userPrivate = userMapperDAO.loadUserPrivateByUserID(userBaseInfo.getId());
        List<Role> roles = userMapperDAO.loadRolesByUserID(userBaseInfo.getId());
        for(Role role : roles){
            role.setBaseRole(userMapperDAO.loadRoleByRoleID(role.getRoleID()));
        }
        User user = new User();
        user.setUserPrivate(userPrivate);
        user.setUserRoles(roles);
        user.setUserBaseInfo(userBaseInfo);
        user.setUserID(userBaseInfo.getId());
        return user;
    }
    public User getCurrentUser(){
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }


    public UserBaseInfo creatUser(RegisterQuery query) {
        if( userMapperDAO.accountExisted(query.getAccount()) >0 ){
            return null;
        }

        UserBaseInfo userBaseInfo = new UserBaseInfo();
        userBaseInfo.setAvator("default");
        userBaseInfo.setName(query.getName());
        userBaseInfo.setPhone(query.getPhone());
        long authTime = System.currentTimeMillis();
        String token = PasswordUtils.encodePassword(authTime +query.getSalt(), query.getSalt());
        userBaseInfo.setToken(token);
        String userID = UUID.randomUUID().toString().replaceAll("-", "t");
        userBaseInfo.setId(userID);
        UserPrivate userPrivate = new UserPrivate();
        userPrivate.setUserID(userID);
        userPrivate.setAccount(query.getAccount());
        userPrivate.setLevel(1);
        userPrivate.setLocked(false);
        userPrivate.setSalt(query.getSalt());
        userPrivate.setPassword(PasswordUtils.encodePassword(query.getPassword(),query.getSalt()));

        userMapperDAO.createBaseUser(userBaseInfo);
        userMapperDAO.createPrivateUser(userPrivate);
        userMapperDAO.createRole(1,userID);

        return userBaseInfo;
    }

    public UserBaseInfo updateAvatar(String path, Authentication auth){
        User user = (User) auth.getPrincipal();
        UserBaseInfo userBaseInfo = user.getUserBaseInfo();
        userBaseInfo.setAvator(path);
        userMapperDAO.updateUserBaseInfo(userBaseInfo);
        return userBaseInfo;
    }

    public UserBaseInfo updateUserBaseInfo(UserBaseInfo baseInfo){
        userMapperDAO.updateUserBaseInfo(baseInfo);
        return baseInfo;
    }

    public void createSecurityQuestion(SecurityQuestion question, String salt){
        String enAnswer = PasswordUtils.encodePassword(question.getAnswer(), salt);
        question.setAnswer(enAnswer);
        userMapperDAO.createSecurityQuestion(question);
    }

    public boolean changePWD(ChangePWDQuery query) {
        List<SecurityQuestion> questions = query.getQuestions();
        for (SecurityQuestion question : questions) {
            SecurityQuestion question1 = userMapperDAO.findQuestion(question.getQuestion(), question.getUserID());
            String answer = question.getAnswer();
            String s = PasswordUtils.encodePassword(answer,query.getUser().getUserPrivate().getSalt());
            if(!question1.getAnswer().equalsIgnoreCase(s)){
                return false;
            }
        }
        UserPrivate userPrivate = query.getUser().getUserPrivate();
        userPrivate.setPassword(query.getNewPWD());
        userMapperDAO.updateUserPrivate(userPrivate);
        return true;
    }

    public boolean hasSecurityProblem(String userId) {
        int num = userMapperDAO.hasSecurityProblem(userId);
        return num == 3;
    }

    public List<SecurityQuestion> checkSecurity(String userId) {

        return userMapperDAO.checkSecurity(userId);
    }

    public boolean hasPaymentPWD(String userID){
        UserPrivate userPrivate = userMapperDAO.loadUserPrivateByUserID(userID);
        return null != userPrivate.getPaymentPWD();
    }

    public boolean validateProblems(List<SecurityQuestion> questions, String salt) {
        List<SecurityQuestion> validator = userMapperDAO.getSecurityProblemsByUserID(questions.get(0).getUserID());
        Collections.sort(questions, new Comparator<SecurityQuestion>() {
            @Override
            public int compare(SecurityQuestion o1, SecurityQuestion o2) {
                return o1.getId() - o2.getId();
            }
        });
        try{
            for (int i = 0; i < 3; i++) {
                SecurityQuestion question = questions.get(i);
                SecurityQuestion validate = validator.get(i);
                String s = PasswordUtils.encodePassword(question.getAnswer(), salt);
                if(!validate.getAnswer().equals(s)){
                    return false;
                }
            }
            long time = System.currentTimeMillis();
            for (SecurityQuestion validate : validator) {
                validate.setChangeTime(time);
                userMapperDAO.updateSecurityQuestion(validate);
            }
            return true;
        }catch (Exception e){
            return false;
        }
    }

    // 大于5 分钟返回false
    public boolean isTryingReset(List<SecurityQuestion> res, String userID) {
        List<SecurityQuestion> questions = userMapperDAO.getSecurityProblemsByUserID(userID);
        SecurityQuestion question = questions.get(0);
        Long changeTime = question.getChangeTime();
        long l = System.currentTimeMillis();
        for(int i = 0; i < 3; i++){
            res.get(i).setId(questions.get(i).getId());
            res.get(i).setChangeTime(0L);
        }
        return l - changeTime < 300000L;
    }

    public void resetSecurityProblems(List<SecurityQuestion> questions,String salt) {
        for (SecurityQuestion question : questions) {
            question.setAnswer(PasswordUtils.encodePassword(question.getAnswer(),salt));
            userMapperDAO.updateSecurityQuestion(question);
        }
    }

    public boolean hasValidated(String userId){
        List<SecurityQuestion> questions = userMapperDAO.getSecurityProblemsByUserID(userId);
        if(questions == null || questions.isEmpty()){
            return true;
        }
        return System.currentTimeMillis() - questions.get(0).getChangeTime() < 300000L;
    }



    /**
     * @param pwd
     * 验证密码是否正确
     * @return 如果密码正确返回true，错误返回false
     */
    public boolean isGoodPWD(String pwd){
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String s = PasswordUtils.encodePassword(pwd, user.getUserPrivate().getSalt());
        return user.getUserPrivate().getPassword().equalsIgnoreCase(s);
    }

    public boolean updatePayment(AddPaymentQuery addPaymentQuery) {
        if(this.hasPaymentPWD(addPaymentQuery.getUser().getUserBaseInfo().getId())) return false;
        if(!this.hasValidated(addPaymentQuery.getUser().getUserID())) return false;
        try {
            String s = PasswordUtils.encodePassword(addPaymentQuery.getPayment(), addPaymentQuery.getUser().getUserPrivate().getSalt());

            userMapperDAO.updatePayment(s, addPaymentQuery.getUser().getUserBaseInfo().getId());
            addPaymentQuery.getUser().getUserPrivate().setPaymentPWD(s);
            this.cancelValidated(addPaymentQuery.getUser().getUserID());
        }catch (Exception e){
            return false;
        }
        return true;
    }

    public boolean resetPayment(ResetPayment resetPayment) {
        if(!this.hasPaymentPWD(resetPayment.getUser().getUserBaseInfo().getId())) return false;
        if(!this.hasValidated(resetPayment.getUser().getUserID())) return false;
        try {
            String s = PasswordUtils.encodePassword(resetPayment.getPayment(), resetPayment.getUser().getUserPrivate().getSalt());
            userMapperDAO.updatePayment(s, resetPayment.getUser().getUserBaseInfo().getId());
            resetPayment.getUser().getUserPrivate().setPaymentPWD(s);
            this.cancelValidated(resetPayment.getUser().getUserID());
        }catch (Exception e){
            return false;
        }
        return true;
    }

    public void cancelValidated(String userId){
        List<SecurityQuestion> questions = userMapperDAO.getSecurityProblemsByUserID(userId);
        for (SecurityQuestion question : questions) {
            question.setChangeTime(0L);
            userMapperDAO.updateSecurityQuestion(question);
        }
    }


    public boolean resetPassword(String password, String account) {
        if(password == null) return false;
        if(account == null){
            User currentUser = this.getCurrentUser();
            if(this.hasValidated(currentUser.getUserBaseInfo().getId())){
                UserPrivate userPrivate = currentUser.getUserPrivate();
                String s = PasswordUtils.encodePassword(password, userPrivate.getSalt());
                userPrivate.setPassword(s);
                userMapperDAO.updateUserPrivate(userPrivate);
                this.cancelValidated(currentUser.getUserBaseInfo().getId());
                return true;
            }
        }else{
            UserPrivate userPrivate = userMapperDAO.lodUserPrivateByUsername(account);
            if(userPrivate == null){
                return false;
            }
            String userID = userPrivate.getUserID();
            if(this.hasValidated(userID)){
                String s = PasswordUtils.encodePassword(password, userPrivate.getSalt());
                userPrivate.setPassword(s);
                userMapperDAO.updateUserPrivate(userPrivate);
                this.cancelValidated(userPrivate.getUserID());
                return true;
            }
        }

        return false;
    }

    public UserPrivate getUserByUserName(String account) {
        return userMapperDAO.lodUserPrivateByUsername(account);
    }

    public UserBaseInfo getUserById(String userId) {
        return userMapperDAO.loadUserBaseInfoByUserID(userId);
    }

    public List<UserAddress> getAddressByUserId(String id) {
        return userMapperDAO.getAddressByUserId(id);
    }

    public int addAddress(UserAddress add) {
        Integer addressMaxId = userMapperDAO.getAddressMaxId();
        addressMaxId = addressMaxId == null ? 1 : addressMaxId+1;
        User currentUser = this.getCurrentUser();
        add.setUserBaseId(currentUser.getUserBaseInfo().getId());
        int count = userMapperDAO.getUserAddressCount(currentUser.getUserID());
        add.setIsDefault(count == 0);
        userMapperDAO.addUserAddress(add);
        return addressMaxId;
    }

    public UserAddress getAddressById(Integer address) {
        return userMapperDAO.getAddressById(address);
    }

    public boolean updateAddress(UserAddress address) {
        Boolean isDefault = address.getIsDefault();
        if(isDefault){
            List<UserAddress > defaultAddresses = userMapperDAO.getDefaultAddress(this.getCurrentUser().getUserID());
            for (UserAddress defaultAddress : defaultAddresses) {
                defaultAddress.setIsDefault(false);
                updateAddress(defaultAddress);
            }
        }
        return 1 == userMapperDAO.updateAddress(address);
    }


    public UserAddress getDefaultAddress() {
        List<UserAddress> defaultAddress = userMapperDAO.getDefaultAddress(this.getCurrentUser().getUserID());

        return defaultAddress == null || defaultAddress.size() == 0 ? null : defaultAddress.get(0);
    }

    public void getGoodsStatus(String goodsId, Map<String,Boolean> data) {
        String userID;
        try {
            userID= this.getCurrentUser().getUserID();
        }catch (Exception e){
            userID = null;
        }
        if(userID == null) return;
        Integer l = userMapperDAO.getGoodsLike(userID,goodsId);
        Integer c = userMapperDAO.getGoodsCollect(userID,goodsId);
        data.put("like",l != null && l >= 1);
        data.put("collect", c != null && c >= 1);
    }

    public void addGood(String goodsId, Map<String, Boolean> data) {
        String userID = this.getCurrentUser().getUserID();
        Integer count = userMapperDAO.addGood(goodsId,userID);
        if(count == null || count == 0){
            return;
        }
        data.put("like",true);
    }

    public void addCollect(String goodsId, Map<String, Boolean> data) {
        String userID = this.getCurrentUser().getUserID();
        Integer count = userMapperDAO.addCollect(goodsId,userID);
        if(count == null || count == 0){
            return;
        }
        data.put("collect",true);

    }

    public void delGood(String goodsId, Map<String, Boolean> data) {
        String userID = this.getCurrentUser().getUserID();
        Integer count = userMapperDAO.delGood(goodsId,userID);
        if(count == null || count == 0){
            return;
        }
        data.put("like",false);
    }

    public void delCollect(String goodsId, Map<String, Boolean> data) {
        String userID = this.getCurrentUser().getUserID();
        Integer count = userMapperDAO.delCollect(goodsId,userID);
        if(count == null || count == 0){
            return;
        }
        data.put("collect",false);
    }
}
