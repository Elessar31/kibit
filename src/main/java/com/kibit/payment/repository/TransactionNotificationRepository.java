package com.kibit.payment.repository;

import com.kibit.payment.entity.TransactionNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionNotificationRepository extends JpaRepository<TransactionNotification, Long> {
}
