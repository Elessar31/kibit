package com.kibit.payment.dto;

import java.math.BigDecimal;

public class TransactionRequest {
    private Long senderAccountId;
    private Long receiverAccountId;
    private BigDecimal amount;

    public Long getSenderAccountId() {
        return this.senderAccountId;
    }

    public Long getReceiverAccountId() {
        return this.receiverAccountId;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setSenderAccountId(Long senderAccountId) {
        this.senderAccountId = senderAccountId;
    }

    public void setReceiverAccountId(Long receiverAccountId) {
        this.receiverAccountId = receiverAccountId;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
