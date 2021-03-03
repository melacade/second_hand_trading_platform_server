package com.second_hand_trading_platform.second_hand_trading_platform.modules.common.dto.input.nomal_query;

import com.sun.xml.internal.fastinfoset.util.CharArray;
import lombok.Data;
import org.apache.logging.log4j.util.Strings;

@Data
public class RegisterQuery {
    private String account;
    private String phone;
    private String password;
    private String name;
    private String salt;

    public boolean isValid() {
        boolean isAccount = Strings.isNotBlank(account);
        boolean isPhone = Strings.isNotBlank(phone);
        boolean isPassword = Strings.isNotBlank(password);
        boolean isName = Strings.isNotBlank(name);
        boolean isSalt = Strings.isNotBlank(salt);
        isAccount = isAccount && account.length() >= 5 && account.length() <= 10 && !account.contains(" ");
        isPhone = isPhone && phone.matches("[1][3578]\\d{9}");
        isPassword = isPassword && password.length() >= 6 && password.length() <= 11;
        isName = isName && name.length() >= 3 && name.length() <= 10;
        isSalt = isSalt && salt.length() >= 7;
        return isAccount && isPhone && isPassword && isName && isSalt;

    }
}
