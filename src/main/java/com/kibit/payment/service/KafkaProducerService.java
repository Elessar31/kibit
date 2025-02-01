package com.kibit.payment.service;

import com.kibit.payment.entity.Transaction;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private static final String TOPIC = "transaction_notifications";


    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendTransactionNotification(Transaction transaction) {
        String message = "Transaction ID: " + transaction.getId() +
                ", Amount: " + transaction.getAmount() +
                ", Status: " + transaction.getStatus();

        kafkaTemplate.send(TOPIC, message);
    }
}
