package com.kibit.payment.service;

import com.kibit.payment.entity.Account;
import com.kibit.payment.exception.AccountNotFoundException;
import com.kibit.payment.repository.AccountRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;
    private final KafkaProducerService kafkaProducerService;

    public AccountService(AccountRepository accountRepository, KafkaProducerService kafkaProducerService) {
        this.accountRepository = accountRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    public Account getAccountById(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
    }

    public Account changeBalance(Long accountId, BigDecimal balance, Long transactionId) {
        Account account = getAccountById(accountId);
        BigDecimal oldBalance = account.getBalance();
        account.setBalance(balance);
        kafkaProducerService.modifyBalanceNotification(account, oldBalance, transactionId);
        return accountRepository.save(account);
    }

}