# Instant Payment API Service

## Overview
This project is an Instant Payment API service built with Spring Boot. The service allows users to send money instantly using REST endpoints. It focuses on **High Availability**, **Transactional Processing**, and **Error Handling**.

The application supports:
- Balance checks before processing transactions.
- Concurrent transaction handling to prevent double-spending or double notifications.
- Transaction logging into a PostgreSQL database.
- Kafka integration for asynchronous notification handling.

---

## Features
- **RESTful API**: Endpoints for user and transaction management.
- **Database**: PostgreSQL for storing accounts and transactions.
- **Concurrency Handling**: Prevents issues like double-spending.
- **Kafka Integration**: Asynchronous notifications using Apache Kafka.
- **Error Handling**: Well-defined exceptions for different scenarios.
- **Dockerized**: Ready for deployment in containerized environments.

---

## Technologies Used
- **Java**: JDK 17
- **Spring Boot**: Framework for building the application.
- **PostgreSQL**: Database for storing accounts and transactions.
- **Apache Kafka**: Message broker for asynchronous notifications.
- **Docker**: For containerizing the application.
- **JUnit 5**: For unit and integration testing.
- **Maven**: Build tool.

---

## Prerequisites
1. **Java 21+**
2. **Docker**
3. **Postman** (optional, for API testing)

---
