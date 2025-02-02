package com.kibit.payment.service;

import com.kibit.payment.entity.Account;
import com.kibit.payment.exception.AccountNotFoundException;
import com.kibit.payment.repository.AccountRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

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



    public Account updateBalance(Long accountId, BigDecimal balance, Long transactionId) {
        Optional<Account> account = accountRepository.findByIdForUpdate(accountId);
        if (account.isPresent()) {
            BigDecimal oldBalance = account.get().getBalance();
            account.get().setBalance(balance);
            kafkaProducerService.modifyBalanceNotification(account.get(), oldBalance, transactionId);
            return accountRepository.save(account.get());
        } else {
            throw new AccountNotFoundException("Account not found");
        }
    }

}