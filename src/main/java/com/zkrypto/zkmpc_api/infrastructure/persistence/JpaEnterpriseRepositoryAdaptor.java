package com.zkrypto.zkmpc_api.infrastructure.persistence;

import com.zkrypto.zkmpc_api.domain.enterprise.domain.entity.Enterprise;
import com.zkrypto.zkmpc_api.domain.enterprise.domain.repository.EnterpriseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;


@Repository
public class JpaEnterpriseRepositoryAdaptor implements EnterpriseRepository {
    private final JpaEnterpriseRepository jpaEnterpriseRepository;

    public JpaEnterpriseRepositoryAdaptor(JpaEnterpriseRepository jpaEnterpriseRepository) {
        this.jpaEnterpriseRepository = jpaEnterpriseRepository;
    }

    @Override
    public Enterprise save(Enterprise enterprise) {
        return jpaEnterpriseRepository.save(enterprise);
    }

    @Override
    public Optional<Enterprise> findByEnterpriseId(String enterpriseId) {
        return jpaEnterpriseRepository.findByEnterpriseId(enterpriseId);
    }

    @Override
    public List<Enterprise> findAll() {
        return jpaEnterpriseRepository.findAll();
    }

    @Override
    public boolean existsByEnterpriseId(String enterpriseId) {
        return jpaEnterpriseRepository.existsByEnterpriseId(enterpriseId);
    }

    @Override
    public List<String> findAllIds() {
        return jpaEnterpriseRepository.findAllIds();
    }
}