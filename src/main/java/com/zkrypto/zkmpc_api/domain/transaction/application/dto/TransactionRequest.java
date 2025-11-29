package com.zkrypto.zkmpc_api.domain.transaction.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequest {
    @NotBlank
    private String from;
    @NotBlank
    private String to;
    @NotNull
    private Double value;
    @NotNull
    private byte[] tx;
}
