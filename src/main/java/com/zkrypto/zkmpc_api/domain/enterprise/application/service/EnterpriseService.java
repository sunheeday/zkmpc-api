package com.zkrypto.zkmpc_api.domain.enterprise.application.service;

import com.zkrypto.zkmpc_api.domain.enterprise.application.dto.EnterpriseRegisterRequest;
import com.zkrypto.zkmpc_api.domain.enterprise.application.dto.EnterpriseResponse;
import com.zkrypto.zkmpc_api.domain.enterprise.domain.entity.Enterprise;
import com.zkrypto.zkmpc_api.domain.enterprise.domain.repository.EnterpriseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnterpriseService {

    @Autowired
    private EnterpriseRepository enterpriseRepository;
//
//    public EnterpriseService(EnterpriseRepository enterpriseRepository) {
//        this.enterpriseRepository = enterpriseRepository;
//    }

    // POST /v1/enterprise
    @Transactional
    public void registerEnterprise(EnterpriseRegisterRequest request) {

        if (enterpriseRepository.existByEnterpriseId(request.getEnterpriseId())) {
            throw new IllegalArgumentException("이미 등록된 ID입니다: " + request.getEnterpriseId()); // 409 Conflict
        }

        // DTO를 Domain Entity로 변환 및 저장
        Enterprise enterprise = new Enterprise(
                request.getEnterpriseId(),
                request.getName()
        );

        enterpriseRepository.save(enterprise);
    }

    // 2. GET /v1/enterprise
    @Transactional(readOnly = true)
    public List<EnterpriseResponse> getAllEnterprises() {

        return enterpriseRepository.findAll().stream()
                .map(EnterpriseResponse::new)
                .collect(Collectors.toList());
    }
}