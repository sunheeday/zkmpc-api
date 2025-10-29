package com.zkrypto.zkmpc_api.domain.enterprise.application.dto;

import com.zkrypto.zkmpc_api.domain.enterprise.domain.entity.Enterprise;
import lombok.Getter;

@Getter
public class EnterpriseResponse {
    private final String enterpriseId;
    private final String enterpriseName;

    public EnterpriseResponse(Enterprise enterprise) {
        this.enterpriseId = enterprise.getEnterpriseId();
        this.enterpriseName = enterprise.getName();
    }
}
