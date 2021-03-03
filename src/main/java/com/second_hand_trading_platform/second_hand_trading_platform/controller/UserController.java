package com.second_hand_trading_platform.second_hand_trading_platform.controller;

import com.second_hand_trading_platform.second_hand_trading_platform.modules.common.dto.input.authed_query.ChangePWDQuery;
import com.second_hand_trading_platform.second_hand_trading_platform.modules.common.dto.input.authed_query.UpdateInfoQuery;
import com.second_hand_trading_platform.second_hand_trading_platform.modules.common.dto.input.nomal_query.RegisterQuery;
import com.second_hand_trading_platform.second_hand_trading_platform.modules.common.dto.output.ApiResult;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.user.SecurityQuestion;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.user.User;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.user.UserBaseInfo;
import com.second_hand_trading_platform.second_hand_trading_platform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    UserService userService;

    @PostMapping("/register")
    ApiResult register(@RequestBody RegisterQuery query){
        if(!query.isValid()){
            return ApiResult.fail("请求信息有误");
        }
        UserBaseInfo userBaseInfo = userService.creatUser(query);
        if (userBaseInfo == null) {
            return ApiResult.fail("创建失败：账户名已被使用！");
        }
        return ApiResult.ok("创建成功！",userBaseInfo);
    }

    @PostMapping("/update")
    ApiResult updateInfo(@RequestBody UpdateInfoQuery query){
        if(query.isValid()){
            return ApiResult.ok("更新成功",userService.updateUserBaseInfo(query.toUserBaseInfo()));
        }else {
            return ApiResult.fail("更新失败");
        }
    }

    @PostMapping("/createSecurityQuestion")
    ApiResult createSecurityQuestion(@RequestBody List<SecurityQuestion> questions){
        if (questions.size() != 3){
            return ApiResult.fail("安全问题数目不正确！");
        }
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = user.getUserBaseInfo().getId();
        if( userService.hasSecurityProblem(userId)){
            return ApiResult.fail("创建失败已经创建过密保问题");
        }

        for (SecurityQuestion question : questions) {
            question.setUserID(userId);
            userService.createSecurityQuestion(question,user.getUserPrivate().getSalt());
        }
        return ApiResult.ok("创建成功");
    }

    @PostMapping("/changePWD")
    ApiResult changePWD(@RequestBody ChangePWDQuery query){
        if (query.isValid()){
            if(userService.changePWD(query)){
                return ApiResult.ok("更改成功");
            }
        }
        return ApiResult.fail("失败");
    }
    
    @GetMapping("/checkSecuriy")
    ApiResult checkSecurity(){
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<SecurityQuestion> questions = userService.checkSecurity(user.getUserBaseInfo().getId());
        if(questions != null && questions.size() == 3){
            return ApiResult.ok("检查成功",questions);
        }
        return ApiResult.fail("没有密保问题");
    }

    @GetMapping("/checkPaymentSecurity")
    ApiResult checkPaymentSecurity(){
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean has = userService.hasPaymentPWD(user.getUserBaseInfo().getId());
        return ApiResult.ok("",has);
    }

    @PostMapping("/validateProblems")
    ApiResult validateProblems(@RequestBody List<SecurityQuestion> questions){
        boolean validate = false;
        if(questions.size() != 3){
            return ApiResult.fail("非法");
        }
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        for (SecurityQuestion question : questions) {
            question.setUserID(user.getUserBaseInfo().getId());
        }
        validate = userService.validateProblems(questions,user.getUserPrivate().getSalt());
        return ApiResult.ok("验证成功",validate);
    }
}
