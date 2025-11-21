package com.zkrypto.zkmpc_api.domain.transaction.domain.repository;

import com.zkrypto.zkmpc_api.domain.transaction.domain.entity.Transaction;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository {
    Transaction save(Transaction transaction);
    Optional<Transaction> findByTransactionId(String transactionId);
    List<Transaction> findAll();
}
