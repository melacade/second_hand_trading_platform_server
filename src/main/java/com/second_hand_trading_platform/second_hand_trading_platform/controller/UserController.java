package com.second_hand_trading_platform.second_hand_trading_platform.controller;

import com.second_hand_trading_platform.second_hand_trading_platform.modules.common.dto.input.authed_query.AddNewSecurityQuestion;
import com.second_hand_trading_platform.second_hand_trading_platform.modules.common.dto.input.authed_query.AddPaymentQuery;
import com.second_hand_trading_platform.second_hand_trading_platform.modules.common.dto.input.authed_query.ResetPayment;
import com.second_hand_trading_platform.second_hand_trading_platform.modules.common.dto.input.authed_query.UpdateInfoQuery;
import com.second_hand_trading_platform.second_hand_trading_platform.modules.common.dto.input.nomal_query.RegisterQuery;
import com.second_hand_trading_platform.second_hand_trading_platform.modules.common.dto.input.nomal_query.ValidateProblems;
import com.second_hand_trading_platform.second_hand_trading_platform.modules.common.dto.output.ApiResult;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.user.SecurityQuestion;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.user.User;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.user.UserAddress;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.user.UserBaseInfo;
import com.second_hand_trading_platform.second_hand_trading_platform.service.UserService;
import com.second_hand_trading_platform.second_hand_trading_platform.utils.PasswordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    UserService userService;

    @PostMapping("/register")
    ApiResult register(@RequestBody RegisterQuery query) {
        if (!query.isValid()) {
            return ApiResult.fail("请求信息有误");
        }
        UserBaseInfo userBaseInfo = userService.creatUser(query);
        if (userBaseInfo == null) {
            return ApiResult.fail("创建失败：账户名已被使用！");
        }
        return ApiResult.ok("创建成功！", userBaseInfo);
    }

    @PostMapping("/update")
    ApiResult updateInfo(@RequestBody UpdateInfoQuery query) {
        if (query.isValid()) {
            return ApiResult.ok("更新成功", userService.updateUserBaseInfo(query.toUserBaseInfo()));
        } else {
            return ApiResult.fail("更新失败");
        }
    }

    @PostMapping("/createSecurityQuestion")
    ApiResult createSecurityQuestion(@RequestBody AddNewSecurityQuestion auth) {
        List<SecurityQuestion> questions = auth.getQuestions();
        if (questions.size() != 3) {
            return ApiResult.fail("安全问题数目不正确！");
        }
        if (auth.getPassword() != null && !PasswordUtils.isValidPassword(auth.getPassword(), auth.getUser().getUserPrivate().getPassword(), auth.getUser().getUserPrivate().getSalt())) {
            return ApiResult.fail("密码不正确");
        }
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = user.getUserBaseInfo().getId();
        if (userService.hasSecurityProblem(userId)) {
            return ApiResult.fail("创建失败已经创建过密保问题");
        }

        for (SecurityQuestion question : questions) {
            question.setUserID(userId);
            userService.createSecurityQuestion(question, user.getUserPrivate().getSalt());
        }
        return ApiResult.ok("创建成功");
    }


    @PostMapping("/resetSecurityProblems")
    ApiResult resetSecurityProblems(@RequestBody List<SecurityQuestion> questions) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!userService.isTryingReset(questions, user.getUserBaseInfo().getId()) || questions.size() != 3) {
            return ApiResult.fail("修改失败");
        } else {
            userService.resetSecurityProblems(questions, user.getUserPrivate().getSalt());
        }
        return ApiResult.ok("修改成功");
    }

    @GetMapping("/checkSecuriy")
    ApiResult checkSecurity(String account) {
        User user;
        if (account == null || account.equalsIgnoreCase("")) {
            user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        } else {
            user = new User();
            user.setUserPrivate(userService.getUserByUserName(account));
            if (user.getUserPrivate() == null) {
                return ApiResult.fail(-1, "没有用户");
            }
        }
        List<SecurityQuestion> questions = userService.checkSecurity(user.getUserPrivate().getUserID());
        if (questions != null && questions.size() == 3) {
            return ApiResult.ok("检查成功", questions);
        }
        return ApiResult.fail("没有密保问题");
    }

    @GetMapping("/checkPaymentSecurity")
    ApiResult checkPaymentSecurity() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean has = userService.hasPaymentPWD(user.getUserBaseInfo().getId());
        return ApiResult.ok("", has);
    }

    @PostMapping("/validateProblems")
    ApiResult validateProblems(@RequestBody ValidateProblems val) {
        List<SecurityQuestion> questions = (List<SecurityQuestion>) val.getQuestions();
        boolean validate = false;
        if (questions.size() != 3) {
            return ApiResult.fail("非法");
        }
        User user;
        if (val.getAccount() == null) {
            user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        } else {
            user = new User();
            user.setUserPrivate(userService.getUserByUserName(val.getAccount()));
        }
        for (SecurityQuestion question : questions) {
            question.setUserID(user.getUserPrivate().getUserID());
        }
        validate = userService.validateProblems(questions, user.getUserPrivate().getSalt());
        return ApiResult.ok("验证成功", validate);
    }

    @PostMapping("/addPayment")
    ApiResult addPayment(@RequestBody AddPaymentQuery addPaymentQuery) {
        if (!addPaymentQuery.isValid()) return ApiResult.fail("不合法的请求");
        if (!userService.isGoodPWD(addPaymentQuery.getPassword())) return ApiResult.fail("密码错误");
        boolean status = userService.updatePayment(addPaymentQuery);
        return status ? ApiResult.ok("添加支付密码成功！", true) : ApiResult.fail("添加失败");
    }

    @PostMapping("/resetPayment")
    ApiResult resetPayment(@RequestBody ResetPayment resetPayment) {
        if (!resetPayment.isValid()) return ApiResult.fail("不合法的请求");
        if (!userService.isGoodPWD(resetPayment.getPassword())) return ApiResult.fail("密码错误");
        String old = PasswordUtils.encodePassword(resetPayment.getOldPayment(), resetPayment.getUser().getUserPrivate().getSalt());
        if (old != null && !old.equalsIgnoreCase(resetPayment.getUser().getUserPrivate().getPaymentPWD()))
            return ApiResult.fail("密码或旧的支付密码错误！");

        boolean status = userService.resetPayment(resetPayment);
        return status ? ApiResult.ok("修改支付密码成功！", true) : ApiResult.fail("添加失败");
    }


    @PostMapping("/resetPassword")
    ApiResult resetPassword(@RequestBody Map<String, String> body) {
        boolean flag = userService.resetPassword(body.get("password"), body.getOrDefault("account", null));
        return flag ? ApiResult.ok("修改成功") : ApiResult.fail("修改失败!");
    }

    @GetMapping("/getAddress")
    ApiResult getAddress() {
        User currentUser = userService.getCurrentUser();
        UserBaseInfo userBaseInfo = currentUser.getUserBaseInfo();
        String id = userBaseInfo.getId();
        List<UserAddress> addressByUserId = userService.getAddressByUserId(id);
        return ApiResult.ok("查询地址成功", addressByUserId);
    }

    @PostMapping("/addUserAddress")
    ApiResult addUserAddress(@RequestBody UserAddress add) {
        if (add.getCountry() != null && add.getCity() != null && add.getProvince() != null && add.getDetail() != null) {
            int id = userService.addAddress(add);
            add.setId(id);
            return ApiResult.ok("添加地址成功", add);
        }
        return ApiResult.fail("非法请求");
    }


    @PostMapping("/updateAddress")
    ApiResult updateAddress(@RequestBody UserAddress address) {
        boolean flag = userService.updateAddress(address);
        return flag ? ApiResult.ok("地址更新成功") : ApiResult.fail("地址更新失败");
    }

    @GetMapping("/getDefaultAddress")
    ApiResult getDefaultAddress() {
        UserAddress defaultAddress = userService.getDefaultAddress();
        return defaultAddress == null ? ApiResult.fail("没有地址") : ApiResult.ok("默认地址查询成功", defaultAddress);
    }


    @GetMapping("/getGoodsStatus/{goodsId}")
    ApiResult getGoodsStatus(@PathVariable("goodsId") String goodsId) {
        Map<String, Boolean> data = new HashMap<>();
        data.put("like", false);
        data.put("collect", false);
        userService.getGoodsStatus(goodsId, data);

        return ApiResult.ok("用户商品状态查询成功！", data);
    }

    @GetMapping("/addGood/{goodsId}")
    ApiResult addGood(@PathVariable("goodsId") String goodsId) {
        Map<String, Boolean> data = new HashMap<>();
        data.put("like", false);
        data.put("collect", false);
        userService.getGoodsStatus(goodsId, data);
        if (!data.get("like")) {
            userService.addGood(goodsId, data);
        } else {
            userService.delGood(goodsId, data);
        }


        return ApiResult.ok("用户商品状态查询成功！", data);
    }

    @GetMapping("/addCollect/{goodsId}")
    ApiResult addCollect(@PathVariable("goodsId") String goodsId) {
        Map<String, Boolean> data = new HashMap<>();
        data.put("like", false);
        data.put("collect", false);
        userService.getGoodsStatus(goodsId, data);
        if (!data.get("collect")) {
            userService.addCollect(goodsId, data);
        } else {
            userService.delCollect(goodsId, data);
        }


        return ApiResult.ok("用户商品状态查询成功！", data);
    }

}
