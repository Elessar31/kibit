package com.kibit.payment.controller;

import com.kibit.payment.entity.Account;
import com.kibit.payment.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The AccountController class is a REST controller responsible for handling HTTP requests
 * related to accounts. It provides endpoints to fetch account details.
 *
 * This controller maps requests to the `/api/accounts` endpoint and works with
 * an AccountService to perform operations involving accounts.
 */
@RestController
@RequestMapping("/api/accounts")
public class AccountController {


    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Handles an HTTP GET request to retrieve an account by its unique identifier.
     *
     * @param id the unique identifier of the account to be retrieved
     * @return a ResponseEntity containing the Account object and an HTTP status code
     */
    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccountById(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccountById(id));
    }

}
