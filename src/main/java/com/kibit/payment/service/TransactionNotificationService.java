package com.kibit.payment.service;

import com.kibit.payment.entity.TransactionNotification;
import com.kibit.payment.repository.TransactionNotificationRepository;
import org.springframework.stereotype.Service;

@Service
public class TransactionNotificationService {
    private final TransactionNotificationRepository transactionNotificationRepository;

    public TransactionNotificationService(TransactionNotificationRepository transactionNotificationRepository) {
        this.transactionNotificationRepository = transactionNotificationRepository;
    }

    public TransactionNotification save(TransactionNotification transactionNotification) {
        return transactionNotificationRepository.save(transactionNotification);
    }
}
