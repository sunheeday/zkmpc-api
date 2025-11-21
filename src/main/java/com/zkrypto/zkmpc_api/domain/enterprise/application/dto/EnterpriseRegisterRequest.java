package com.zkrypto.zkmpc_api.domain.enterprise.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Setter
public class EnterpriseRegisterRequest {
    @NotBlank
    private String enterpriseId;
    @NotBlank
    private String name;
}
