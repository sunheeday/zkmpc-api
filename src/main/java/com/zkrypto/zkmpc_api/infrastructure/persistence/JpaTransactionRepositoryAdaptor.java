package com.zkrypto.zkmpc_api.infrastructure.persistence;

import com.zkrypto.zkmpc_api.domain.transaction.domain.entity.Transaction;
import com.zkrypto.zkmpc_api.domain.transaction.domain.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaTransactionRepositoryAdaptor implements TransactionRepository {
    private final JpaTransactionRepository jpaTransactionRepository;

    public JpaTransactionRepositoryAdaptor(JpaTransactionRepository jpaTransactionRepository) {
        this.jpaTransactionRepository = jpaTransactionRepository;
    }

    @Override
    public Transaction save(Transaction transaction) {
        return jpaTransactionRepository.save(transaction);
    }

    @Override
    public Optional<Transaction> findByTransactionId(String transactionId) {
        return jpaTransactionRepository.findByTransactionId(transactionId);
    }

    @Override
    public List<Transaction> findAll() {
        return jpaTransactionRepository.findAll();
    }
}