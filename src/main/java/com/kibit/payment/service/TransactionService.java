package com.kibit.payment.service;

import com.kibit.payment.dto.TransactionRequest;
import com.kibit.payment.entity.Account;
import com.kibit.payment.entity.Transaction;
import com.kibit.payment.entity.TransactionNotification;
import com.kibit.payment.entity.TransactionStatus;
import com.kibit.payment.exception.AccountNotFoundException;
import com.kibit.payment.exception.InsufficientBalanceException;
import com.kibit.payment.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


/**
 * Service for processing and managing transactions.
 * This service handles creation, validation, and completion of monetary transactions
 * between accounts. It also integrates with additional services for account management,
 * Kafka message production, and transaction notifications.
 *
 * Dependencies:
 * - TransactionRepository for persisting and retrieving transaction data.
 * - AccountService for retrieving account information and updating account balances.
 * - KafkaProducerService for producing messages to Kafka topics.
 * - TransactionNotificationService for creating and saving transaction notifications.
 *
 * Transaction processing steps include:
 * 1. Validating the transaction request.
 * 2. Ensuring sender has sufficient balance.
 * 3. Handling currency conversions if sender and receiver's account currencies differ.
 * 4. Creating and saving transaction records.
 * 5. Performing post-transaction actions such as updating account balances and producing notifications.
 */
@Service
@Transactional
@Slf4j
public class TransactionService {


    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final KafkaProducerService kafkaProducerService;
    private final TransactionNotificationService transactionNotificationService;



    /**
     * Constructs the TransactionService with the required dependencies.
     *
     * @param transactionRepository the repository for managing transaction entities
     * @param accountService the service for managing account operations
     * @param kafkaProducerService the service for producing Kafka messages
     * @param transactionNotificationService the service for managing transaction notifications
     */
    public TransactionService(TransactionRepository transactionRepository, AccountService accountService, KafkaProducerService kafkaProducerService, TransactionNotificationService transactionNotificationService) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
        this.kafkaProducerService = kafkaProducerService;
        this.transactionNotificationService = transactionNotificationService;
    }

    public Transaction processTransaction(TransactionRequest request) {
        validateRequest(request);

        Account sender = accountService.getAccountById(request.getSenderAccountId());
        if (sender == null) {
            log.error("Sender account does not exist");
            throw new AccountNotFoundException("Sender account does not exist");
        }

        Account receiver = accountService.getAccountById(request.getReceiverAccountId());
        if (receiver == null) {
            log.error("Receiver account does not exist");
            throw new AccountNotFoundException("Receiver account does not exist");
        }


        validateSenderBalance(sender, request.getAmount());

        String currencyChanged = handleCurrencyConversion(sender, receiver, request);

        Transaction transaction = createAndSaveTransaction(sender, receiver, request.getAmount());
        handlePostTransactionActions(transaction, sender, receiver, request.getAmount(), currencyChanged);

        return transaction;
    }

    private void validateRequest(TransactionRequest request) {
        if (request == null) {
            log.error("Transaction request cannot be null");
            throw new IllegalArgumentException("Transaction request cannot be null");
        }
        if (request.getSenderAccountId() == null || request.getReceiverAccountId() == null) {
            log.error("Sender and receiver account IDs cannot be null");
            throw new IllegalArgumentException("Sender and receiver account IDs cannot be null");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Transaction amount must be greater than zero");
            throw new IllegalArgumentException("Transaction amount must be greater than zero");
        }
        if (request.getSenderAccountId().equals(request.getReceiverAccountId())) {
            log.error("Sender and receiver cannot be the same");
            throw new IllegalArgumentException("Sender and receiver cannot be the same");
        }
    }

    private void validateSenderBalance(Account sender, BigDecimal amount) {
        if (sender.getBalance().compareTo(amount) < 0) {
            log.error("Sender balance is less than the requested amount");
            throw new InsufficientBalanceException("Sender balance is less than the requested amount");
        }
    }

    private String handleCurrencyConversion(Account sender, Account receiver, TransactionRequest request) {
        if (!sender.getCurrency().equalsIgnoreCase(receiver.getCurrency())) {
            return convertCurrencies(request, sender.getCurrency(), receiver.getCurrency());
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
        transactionNotification.setMessage("Transaction ID: " + transaction.getId() + " has been completed");
        transactionNotificationService.save(transactionNotification);
    }

    private String convertCurrencies(TransactionRequest request, String oldCurrency, String newCurrency) {
        request.setAmount(request.getAmount().multiply(BigDecimal.valueOf(1.1)));
        return ": Amount converted from " + oldCurrency + " to " + newCurrency;
    }
}
