package com.kibit.payment.service;

import com.kibit.payment.dto.TransactionRequest;
import com.kibit.payment.entity.Account;
import com.kibit.payment.entity.Transaction;
import com.kibit.payment.entity.User;
import com.kibit.payment.exception.InsufficientBalanceException;
import com.kibit.payment.repository.TransactionRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;


@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private AccountService accountService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionNotificationService transactionNotificationService;

    @Mock
    private KafkaProducerService kafkaProducerService;
    private Account sender = new Account();
    private Account receiver = new Account();

    @BeforeEach
    void setUp() {
        User senderUser = new User();
        senderUser.setEmail("sender@email.com");
        senderUser.setName("sender");
        User receiverUser = new User();
        receiverUser.setEmail("receiver@email.com");
        receiverUser.setName("receiver");


        sender.setId(1L);
        sender.setBalance(new BigDecimal("500.00"));
        sender.setCurrency("USD");
        sender.setUser(senderUser);

        receiver.setId(2L);
        receiver.setBalance(new BigDecimal("100.00"));
        receiver.setCurrency("EUR");
        receiver.setUser(receiverUser);
    }

    @Test
    void processTransaction_success() {
        // Arrange
        TransactionRequest request = new TransactionRequest();
        request.setSenderAccountId(1L);
        request.setReceiverAccountId(2L);
        request.setAmount(new BigDecimal("100.00"));

        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setSenderAccount(sender);
        transaction.setReceiverAccount(receiver);
        transaction.setAmount(new BigDecimal("100.00"));
        sender.setCurrency(receiver.getCurrency());

        Mockito.when(accountService.getAccountById(1L)).thenReturn(sender);
        Mockito.when(accountService.getAccountById(2L)).thenReturn(receiver);
        Mockito.when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transaction);

        // Act
        Transaction result = transactionService.processTransaction(request);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(transaction.getId(), result.getId());
        Assertions.assertEquals(new BigDecimal("100.00"), result.getAmount());

        // Verify interactions
        Mockito.verify(accountService).getAccountById(1L);
        Mockito.verify(accountService).getAccountById(2L);
        Mockito.verify(accountService).updateBalance(sender.getId(), new BigDecimal("400.00"), transaction.getId());
        Mockito.verify(accountService).updateBalance(receiver.getId(), new BigDecimal("200.00"), transaction.getId());
        Mockito.verify(kafkaProducerService).sendTransactionNotification(transaction);
    }

    @Test
    void processTransaction_insufficientBalance_throwsException() {
        // Arrange

        TransactionRequest request = new TransactionRequest();
        request.setSenderAccountId(1L);
        request.setReceiverAccountId(2L);
        request.setAmount(new BigDecimal("600.00"));


        Mockito.when(accountService.getAccountById(1L)).thenReturn(sender);

        // Act & Assert
        InsufficientBalanceException exception = Assertions.assertThrows(
                InsufficientBalanceException.class,
                () -> transactionService.processTransaction(request)
        );

        Assertions.assertEquals("Sender balance is less than the requested amount", exception.getMessage());
        Mockito.verify(accountService).getAccountById(1L);
        Mockito.verifyNoInteractions(transactionRepository, kafkaProducerService);
    }

    @Test
    void processTransaction_sameSenderReceiver_throwsException() {
        // Arrange

        TransactionRequest request = new TransactionRequest();
        request.setSenderAccountId(1L);
        request.setReceiverAccountId(1L);
        request.setAmount(new BigDecimal("100.00"));

        // Act & Assert
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.processTransaction(request)
        );

        Assertions.assertEquals("Sender and receiver cannot be the same", exception.getMessage());
        Mockito.verifyNoInteractions(accountService, transactionRepository, kafkaProducerService);
    }

    @Test
    void processTransaction_currencyConversion() {
        // Arrange

        TransactionRequest request = new TransactionRequest();
        request.setSenderAccountId(1L);
        request.setReceiverAccountId(2L);
        request.setAmount(new BigDecimal("100.00"));

        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setSenderAccount(sender);
        transaction.setReceiverAccount(receiver);
        transaction.setAmount(new BigDecimal("100.00"));

        Mockito.when(accountService.getAccountById(1L)).thenReturn(sender);
        Mockito.when(accountService.getAccountById(2L)).thenReturn(receiver);
        Mockito.when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transaction);

        Mockito.doNothing().when(kafkaProducerService).modifyCurrencyNotification(Mockito.anyString());

        // Act
        Transaction result = transactionService.processTransaction(request);

        // Assert
        Assertions.assertNotNull(result);
        Mockito.verify(kafkaProducerService).modifyCurrencyNotification(Mockito.contains("Amount converted from"));
    }
}
