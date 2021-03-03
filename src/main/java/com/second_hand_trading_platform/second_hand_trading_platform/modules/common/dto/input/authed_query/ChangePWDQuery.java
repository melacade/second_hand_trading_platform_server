package com.second_hand_trading_platform.second_hand_trading_platform.modules.common.dto.input.authed_query;

import com.second_hand_trading_platform.second_hand_trading_platform.pojo.entity.user.SecurityQuestion;
import lombok.Data;
import org.apache.logging.log4j.util.Strings;

import java.util.List;

@Data
public class ChangePWDQuery extends AuthQuery {
    private String newPWD;
    private List<SecurityQuestion> questions;

    public boolean isValid() {
        init();
        boolean isPassword = Strings.isNotBlank(newPWD);
        boolean isQuestions = questions != null && questions.size() == 3;
        isPassword = isPassword ? newPWD.length() >= 6 && newPWD.length() <= 11 : isPassword;
        return isPassword && isQuestions;
    }
    public void init() {
        if(questions == null)
            return;
        for (SecurityQuestion question : questions) {
            question.setUserID(this.getUser().getUserBaseInfo().getId());
        }
    }
}
