package com.zkrypto.zkmpc_api.domain.transaction.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionStatusUpdateRequest {
    @NotBlank
    private String transactionId; // 거래 요청 ID
    private String txId; // 트랜잭션 해시

    @NotNull
    private Double vat; // 트랜잭션 가스비
}
