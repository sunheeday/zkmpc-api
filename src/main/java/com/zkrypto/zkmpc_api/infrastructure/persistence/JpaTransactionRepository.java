package com.zkrypto.zkmpc_api.infrastructure.persistence;

import com.zkrypto.zkmpc_api.domain.transaction.domain.entity.Transaction;
import com.zkrypto.zkmpc_api.domain.transaction.domain.repository.TransactionRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaTransactionRepository extends JpaRepository<Transaction, Long> { // PK 타입은 Long (id)
    Optional<Transaction> findByTransactionId(String transactionId);
}