package com.second_hand_trading_platform.second_hand_trading_platform.modules.common.dto.input.authed_query;

import lombok.Data;

@Data
public class ResetPayment extends AddPaymentQuery{
    private String oldPayment;

}
