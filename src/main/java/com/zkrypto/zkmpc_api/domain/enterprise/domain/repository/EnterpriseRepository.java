package com.zkrypto.zkmpc_api.domain.enterprise.domain.repository;

import com.zkrypto.zkmpc_api.domain.enterprise.domain.entity.Enterprise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface EnterpriseRepository {
    Enterprise save(Enterprise enterprise);
    Optional<Enterprise> findByEnterpriseId(String enterpriseId);
    List<Enterprise> findAll();
    boolean existByEnterpriseId(String enterpriseId);
}