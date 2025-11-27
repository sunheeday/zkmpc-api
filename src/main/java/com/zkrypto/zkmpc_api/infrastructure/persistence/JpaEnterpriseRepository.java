package com.zkrypto.zkmpc_api.infrastructure.persistence;

import com.zkrypto.zkmpc_api.domain.enterprise.domain.entity.Enterprise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface JpaEnterpriseRepository extends JpaRepository<Enterprise, String> { // PK 타입이 String (enterpriseId)
    Optional<Enterprise> findByEnterpriseId(String enterpriseId);
    boolean existsByEnterpriseId(String enterpriseId);
    @Query("SELECT e.enterpriseId FROM Enterprise e")
    List<String> findAllIds();
}
