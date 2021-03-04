package com.second_hand_trading_platform.second_hand_trading_platform.service;

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

    public boolean isGoodPWD(String pwd){
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String s = PasswordUtils.encodePassword(pwd, user.getUserPrivate().getSalt());
        return user.getUserPrivate().getPassword().equalsIgnoreCase(s);
    }

    public boolean updatePayment(AddPaymentQuery addPaymentQuery) {
        if(this.hasPaymentPWD(addPaymentQuery.getUser().getUserBaseInfo().getId())) return false;
        try {
            String s = PasswordUtils.encodePassword(addPaymentQuery.getPayment(), addPaymentQuery.getUser().getUserPrivate().getSalt());

            userMapperDAO.updatePayment(s, addPaymentQuery.getUser().getUserBaseInfo().getId());
            addPaymentQuery.getUser().getUserPrivate().setPaymentPWD(s);
        }catch (Exception e){
            return false;
        }
        return true;
    }

    public boolean resetPayment(ResetPayment resetPayment) {
        if(!this.hasPaymentPWD(resetPayment.getUser().getUserBaseInfo().getId())) return false;
        try {
            String s = PasswordUtils.encodePassword(resetPayment.getPayment(), resetPayment.getUser().getUserPrivate().getSalt());
            userMapperDAO.updatePayment(s, resetPayment.getUser().getUserBaseInfo().getId());
            resetPayment.getUser().getUserPrivate().setPaymentPWD(s);
        }catch (Exception e){
            return false;
        }
        return true;
    }
}
