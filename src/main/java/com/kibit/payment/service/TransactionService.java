package com.kibit.payment.service;

import com.kibit.payment.dto.TransactionRequest;
import com.kibit.payment.entity.Account;
import com.kibit.payment.entity.Transaction;
import com.kibit.payment.entity.TransactionStatus;
import com.kibit.payment.exception.InsufficientBalanceException;
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

    public Transaction processTransaction(TransactionRequest request) {
        if (request.getSenderAccountId().equals(request.getReceiverAccountId())) {
            throw new RuntimeException("Sender and receiver cannot be the same");
        }

        String currencyChanged = "";

        Account sender = accountService.getAccountById(request.getSenderAccountId());
        Account receiver = accountService.getAccountById(request.getReceiverAccountId());

        BigDecimal amount = request.getAmount();
        if (sender.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Sender balance is less than the requested amount");
        }

        if (!sender.getCurrency().equalsIgnoreCase(receiver.getCurrency())) {
            currencyChanged = convertCurrencies(amount, sender.getCurrency(), receiver.getCurrency());
        }


        Transaction transaction = new Transaction();
        transaction.setSenderAccount(sender);
        transaction.setReceiverAccount(receiver);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.COMPLETED);
        Transaction savedTransaction = transactionRepository.save(transaction);

        accountService.changeBalance(sender.getId(), sender.getBalance().subtract(amount), savedTransaction.getId());
        accountService.changeBalance(receiver.getId(), receiver.getBalance().add(amount), savedTransaction.getId());

        kafkaProducerService.sendTransactionNotification(savedTransaction);
        kafkaProducerService.modifyCurrencyNotification(savedTransaction.getId() + currencyChanged);
        return savedTransaction;
    }

    private String convertCurrencies(BigDecimal amount, String oldCurrency, String newCurrency) {
        amount = amount.multiply(new BigDecimal(1.01));
        return ": Amount converted from" + oldCurrency + " to " + newCurrency;
    }
}
