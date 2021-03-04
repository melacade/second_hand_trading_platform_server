package com.second_hand_trading_platform.second_hand_trading_platform.modules.common.dto.input.authed_query;


import lombok.Data;

@Data
public class AddPaymentQuery extends AuthQuery{
    String password;
    String payment;

    public boolean isValid(){
        return password != null && payment != null && payment.length() == 6;
    }
}
