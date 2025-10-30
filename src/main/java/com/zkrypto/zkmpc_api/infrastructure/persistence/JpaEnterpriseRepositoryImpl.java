package com.zkrypto.zkmpc_api.infrastructure.persistence;

import com.zkrypto.zkmpc_api.domain.enterprise.domain.entity.Enterprise;
import com.zkrypto.zkmpc_api.domain.enterprise.domain.repository.EnterpriseRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;


@Repository
public class JpaEnterpriseRepositoryImpl implements EnterpriseRepository {
    private final JpaEnterpriseRepository jpaRepository;

    public JpaEnterpriseRepositoryImpl(JpaEnterpriseRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Enterprise save(Enterprise enterprise) {
        return jpaRepository.save(enterprise);
    }

    @Override
    public Optional<Enterprise> findByEnterpriseId(String enterpriseId) {
        return jpaRepository.findByEnterpriseId(enterpriseId);
    }

    @Override
    public List<Enterprise> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public boolean existByEnterpriseId(String enterpriseId) {
        return jpaRepository.existsByEnterpriseId(enterpriseId);
    }
}