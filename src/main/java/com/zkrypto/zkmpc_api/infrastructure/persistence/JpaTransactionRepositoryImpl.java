package com.zkrypto.zkmpc_api.infrastructure.persistence;

import com.zkrypto.zkmpc_api.domain.transaction.domain.entity.Transaction;
import com.zkrypto.zkmpc_api.domain.transaction.domain.repository.TransactionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaTransactionRepositoryImpl implements TransactionRepository {
    private final JpaTransactionRepository jpaRepository;

    public JpaTransactionRepositoryImpl(JpaTransactionRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Transaction save(Transaction transaction) {
        return jpaRepository.save(transaction);
    }

    @Override
    public Optional<Transaction> findByTransactionId(String transactionId) {
        return jpaRepository.findByTransactionId(transactionId);
    }

    @Override
    public List<Transaction> findAll() {
        return jpaRepository.findAll();
    }
}
