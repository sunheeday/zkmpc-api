package com.zkrypto.zkmpc_api.domain.enterprise.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnterpriseRegisterRequest {
    @NotBlank
    private String enterpriseId;
    @NotBlank
    private String name;
}
