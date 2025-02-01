package com.kibit.payment.controller;

import com.kibit.payment.dto.TransactionRequest;
import com.kibit.payment.entity.Transaction;
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
     * Creates and processes a new transaction based on the provided request data.
     *
     * @param request the transaction request object containing the sender account ID,
     *                receiver account ID, and the transaction amount
     * @return a ResponseEntity containing the created Transaction object with a
     *         status of HttpStatus.CREATED
     */
    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@RequestBody TransactionRequest request) {
        Transaction transaction = transactionService.processTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }
}
