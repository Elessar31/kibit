package com.kibit.payment.service;

import com.kibit.payment.dto.TransactionRequest;
import com.kibit.payment.entity.Account;
import com.kibit.payment.entity.Transaction;
import com.kibit.payment.entity.TransactionNotification;
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
    private final TransactionNotificationService transactionNotificationService;

    public TransactionService(TransactionRepository transactionRepository, AccountService accountService, KafkaProducerService kafkaProducerService, TransactionNotificationService transactionNotificationService) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
        this.kafkaProducerService = kafkaProducerService;
        this.transactionNotificationService = transactionNotificationService;
    }

    public Transaction processTransaction(TransactionRequest request) {
        validateRequest(request);

        Account sender = accountService.getAccountById(request.getSenderAccountId());
        Account receiver = accountService.getAccountById(request.getReceiverAccountId());

        BigDecimal amount = request.getAmount();
        validateSenderBalance(sender, amount);

        String currencyChanged = handleCurrencyConversion(sender, receiver, amount);

        Transaction transaction = createAndSaveTransaction(sender, receiver, amount);
        handlePostTransactionActions(transaction, sender, receiver, amount, currencyChanged);

        return transaction;
    }

    private void validateRequest(TransactionRequest request) {
        if (request.getSenderAccountId().equals(request.getReceiverAccountId())) {
            throw new IllegalArgumentException("Sender and receiver cannot be the same");
        }
    }

    private void validateSenderBalance(Account sender, BigDecimal amount) {
        if (sender.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Sender balance is less than the requested amount");
        }
    }

    private String handleCurrencyConversion(Account sender, Account receiver, BigDecimal amount) {
        if (!sender.getCurrency().equalsIgnoreCase(receiver.getCurrency())) {
            return convertCurrencies(amount, sender.getCurrency(), receiver.getCurrency());
        }
        return "";
    }

    private Transaction createAndSaveTransaction(Account sender, Account receiver, BigDecimal amount) {
        Transaction transaction = createTransaction(sender, receiver, amount);
        return transactionRepository.save(transaction);
    }

    private void handlePostTransactionActions(Transaction transaction, Account sender, Account receiver, BigDecimal amount, String currencyChanged) {
        saveTransactionNotification(transaction, receiver.getUser().getEmail());

        accountService.updateBalance(sender.getId(), sender.getBalance().subtract(amount), transaction.getId());
        accountService.updateBalance(receiver.getId(), receiver.getBalance().add(amount), transaction.getId());

        kafkaProducerService.sendTransactionNotification(transaction);

        if (!currencyChanged.isEmpty()) {
            kafkaProducerService.modifyCurrencyNotification(transaction.getId() + currencyChanged);
        }
    }


    private Transaction createTransaction(Account sender, Account receiver, BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.setSenderAccount(sender);
        transaction.setReceiverAccount(receiver);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.COMPLETED);
        return transaction;
    }

    private void saveTransactionNotification(Transaction transaction, String receiverEmail) {
        TransactionNotification transactionNotification = new TransactionNotification();
        transactionNotification.setTransaction(transaction);
        transactionNotification.setRecipientEmail(receiverEmail);
        transactionNotificationService.save(transactionNotification);
    }

    private String convertCurrencies(BigDecimal amount, String oldCurrency, String newCurrency) {
        amount = amount.multiply(new BigDecimal(1.01));
        return ": Amount converted from" + oldCurrency + " to " + newCurrency;
    }
}
