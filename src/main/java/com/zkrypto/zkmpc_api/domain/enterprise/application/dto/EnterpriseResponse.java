package com.zkrypto.zkmpc_api.domain.enterprise.application.dto;

import com.zkrypto.zkmpc_api.domain.enterprise.domain.entity.Enterprise;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EnterpriseResponse {
    private String enterpriseId;
    private String enterpriseName;

    public EnterpriseResponse(Enterprise enterprise) {
        this.enterpriseId = enterprise.getEnterpriseId();
        this.enterpriseName = enterprise.getName();
    }
}
