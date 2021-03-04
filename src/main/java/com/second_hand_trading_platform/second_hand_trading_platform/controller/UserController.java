package com.second_hand_trading_platform.second_hand_trading_platform.controller;

import com.second_hand_trading_platform.second_hand_trading_platform.modules.common.dto.input.authed_query.*;
import com.second_hand_trading_platform.second_hand_trading_platform.modules.common.dto.input.nomal_query.RegisterQuery;
import com.second_hand_trading_platform.second_hand_trading_platform.modules.common.dto.output.ApiResult;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.user.SecurityQuestion;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.user.User;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.user.UserBaseInfo;
import com.second_hand_trading_platform.second_hand_trading_platform.service.UserService;
import com.second_hand_trading_platform.second_hand_trading_platform.utils.PasswordUtils;
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
    ApiResult createSecurityQuestion(@RequestBody AddNewSecurityQuestion auth){
        List<SecurityQuestion> questions = auth.getQuestions();
        if (questions.size() != 3){
            return ApiResult.fail("安全问题数目不正确！");
        }
        if (auth.getPassword() != null&&!PasswordUtils.isValidPassword(auth.getPassword(), auth.getUser().getUserPrivate().getPassword(), auth.getUser().getUserPrivate().getSalt())){
            return ApiResult.fail("密码不正确");
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


    @PostMapping("/resetSecurityProblems")
    ApiResult resetSecurityProblems(@RequestBody List<SecurityQuestion> questions){
        User user =(User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!userService.isTryingReset(questions,user.getUserBaseInfo().getId())|| questions.size() != 3){
            return ApiResult.fail("修改失败");
        }else{
            userService.resetSecurityProblems(questions,user.getUserPrivate().getSalt());
        }
        return ApiResult.ok("修改成功");
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

    @PostMapping("/addPayment")
    ApiResult addPayment(@RequestBody AddPaymentQuery addPaymentQuery){
        if(!addPaymentQuery.isValid()) return ApiResult.fail("不合法的请求");
        if(!userService.isGoodPWD(addPaymentQuery.getPassword())) return ApiResult.fail("密码错误");
        boolean status = userService.updatePayment(addPaymentQuery);
        return status ? ApiResult.ok("添加支付密码成功！", true) : ApiResult.fail("添加失败");
    }
    @PostMapping("/resetPayment")
    ApiResult resetPayment(@RequestBody ResetPayment resetPayment){
        if(!resetPayment.isValid()) return ApiResult.fail("不合法的请求");
        if(!userService.isGoodPWD(resetPayment.getPassword())) return ApiResult.fail("密码错误");
        String old = PasswordUtils.encodePassword(resetPayment.getOldPayment(), resetPayment.getUser().getUserPrivate().getSalt());
        if(old!=null&&!old.equalsIgnoreCase(resetPayment.getUser().getUserPrivate().getPaymentPWD())) return ApiResult.fail("密码或旧的支付密码错误！");

        boolean status = userService.resetPayment(resetPayment);
        return status ? ApiResult.ok("修改支付密码成功！", true) : ApiResult.fail("添加失败");
    }
}
