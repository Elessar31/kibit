package com.kibit.payment.dto;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class TransactionRequest {
    private Long senderAccountId;
    private Long receiverAccountId;
    private BigDecimal amount;

}
