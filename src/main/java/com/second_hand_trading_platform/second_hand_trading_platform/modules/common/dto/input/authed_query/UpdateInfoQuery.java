package com.second_hand_trading_platform.second_hand_trading_platform.modules.common.dto.input.authed_query;

import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.user.User;
import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.user.UserBaseInfo;
import lombok.Data;
import org.apache.logging.log4j.util.Strings;

@Data
public class UpdateInfoQuery extends AuthQuery{
    private String name;
    private String phone;

    public boolean isValid(){
        boolean isName = Strings.isNotBlank(name);
        boolean isPhone = Strings.isNotBlank(phone);
        isName = isName ? name.length() >= 3 && name.length() <= 10 : isName;
        isPhone = isPhone ? phone.matches("[1][3578]\\d{9}") : isPhone;
        return isName && isPhone;
    }

    public UserBaseInfo toUserBaseInfo(){
        User user = this.getUser();
        UserBaseInfo userBaseInfo = user.getUserBaseInfo();
        userBaseInfo.setPhone(phone);
        userBaseInfo.setPhone(phone);
        return userBaseInfo;
    }
}
