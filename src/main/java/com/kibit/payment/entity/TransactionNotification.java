package com.kibit.payment.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.util.Date;

@Entity
@Table(name = "transaction_notifications")
public class TransactionNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @Column(nullable = false, length = 100)
    private String recipientEmail;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date sentAt = new Date();

    public Long getId() {
        return this.id;
    }

    public Transaction getTransaction() {
        return this.transaction;
    }

    public String getRecipientEmail() {
        return this.recipientEmail;
    }

    public String getMessage() {
        return this.message;
    }

    public Date getSentAt() {
        return this.sentAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSentAt(Date sentAt) {
        this.sentAt = sentAt;
    }
}
