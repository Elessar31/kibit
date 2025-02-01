package com.kibit.payment.service;

import com.kibit.payment.entity.Transaction;
import com.kibit.payment.entity.TransactionStatus;
import com.kibit.payment.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Transactional
public class TransactionService {


    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final KafkaProducerService kafkaProducerService;

    public TransactionService(TransactionRepository transactionRepository, AccountService accountService, KafkaProducerService kafkaProducerService) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
        this.kafkaProducerService = kafkaProducerService;
    }

    public Transaction processTransaction(Long senderId, Long receiverId, BigDecimal amount) {
        if (senderId.equals(receiverId)) {
            throw new RuntimeException("Sender and receiver cannot be the same");
        }


        Transaction transaction = new Transaction();
        transaction.setSenderAccount(accountService.getAccountById(senderId));
        transaction.setReceiverAccount(accountService.getAccountById(receiverId));
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.COMPLETED);

        Transaction savedTransaction = transactionRepository.save(transaction);

        // Küldjük az értesítést Kafka-n keresztül
        kafkaProducerService.sendTransactionNotification(savedTransaction);

        return savedTransaction;
    }
}
