package com.kibit.payment.service;

import com.kibit.payment.entity.Account;
import com.kibit.payment.entity.Transaction;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class KafkaProducerService {

    private static final String TRANSACTION_TOPIC = "transaction_notifications";
    private static final String ACCOUNT_TOPIC = "account_notifications";
    public static final String CURRENCY_TOPIC = "currency_notifications";


    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendTransactionNotification(Transaction transaction) {
        String message = "Transaction ID: " + transaction.getId() +
                ", Amount: " + transaction.getAmount() +
                ", Status: " + transaction.getStatus();

        kafkaTemplate.send(TRANSACTION_TOPIC, message);
    }

    public void modifyBalanceNotification(Account account, BigDecimal oldBalance, Long transactionId) {
        String message = transactionId + ": Account balance changed to: " + account.getBalance() +
                ", Old balance: " + oldBalance;
        kafkaTemplate.send(ACCOUNT_TOPIC, message);
    }

    public void modifyCurrencyNotification(String message) {
        kafkaTemplate.send(CURRENCY_TOPIC, message);
    }
}
