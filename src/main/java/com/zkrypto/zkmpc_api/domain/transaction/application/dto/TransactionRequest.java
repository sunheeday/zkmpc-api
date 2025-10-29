package com.zkrypto.zkmpc_api.domain.transaction.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionRequest {
    @NotBlank
    private String from;
    @NotBlank
    private String to;

    @NotNull
    private Double value;

}
