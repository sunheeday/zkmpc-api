package com.zkrypto.zkmpc_api.domain.transaction.application.dto;

import com.zkrypto.zkmpc_api.domain.transaction.domain.entity.Transaction;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class TransactionResponse {
    private final String transactionId;
    private final String from;
    private final String to;
    private final Double value;
    private final String status;
    private final String txId;
    private final LocalDateTime createdAt; // createdAt을 사용한다고 가정

    public TransactionResponse(Transaction transaction) {
        this.transactionId = transaction.getTransactionId();
        this.from = transaction.getSender();
        this.to = transaction.getReceiver();
        this.value = transaction.getValue();
        this.status = transaction.getStatus().name();
        this.txId = transaction.getTxId();
        this.createdAt = transaction.getCreatedAt();
    }
}
