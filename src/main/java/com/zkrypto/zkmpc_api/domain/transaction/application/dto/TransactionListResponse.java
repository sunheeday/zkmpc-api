package com.zkrypto.zkmpc_api.domain.transaction.application.dto;

import lombok.Getter;

import java.util.List;
@Getter
public class TransactionListResponse {
    private final List<TransactionResponse> transactions;
    public TransactionListResponse(List<TransactionResponse> transactions) {
        this.transactions = transactions;
    }
}
