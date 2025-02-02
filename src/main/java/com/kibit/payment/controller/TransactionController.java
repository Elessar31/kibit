package com.kibit.payment.controller;

import com.kibit.payment.dto.TransactionRequest;
import com.kibit.payment.entity.Transaction;
import com.kibit.payment.security.PreAuthorize;
import com.kibit.payment.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {


    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }


    /**
     * Creates a new transaction based on the provided transaction request.
     * This method processes the transaction and returns the created transaction
     * wrapped in a ResponseEntity with an HTTP status of 201 (Created).
     *
     * @param request the transaction request containing details such as sender account ID,
     *                receiver account ID, and the transfer amount
     * @return a ResponseEntity containing the created Transaction object and the HTTP status
     */
    @PostMapping
    @PreAuthorize()
    public ResponseEntity<Transaction> createTransaction(@RequestBody TransactionRequest request) {
        Transaction transaction = transactionService.processTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }
}
