package com.kibit.payment.repository;

import com.kibit.payment.entity.Account;
import com.kibit.payment.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findBySenderAccountOrReceiverAccount(Account sender, Account receiver);
}
