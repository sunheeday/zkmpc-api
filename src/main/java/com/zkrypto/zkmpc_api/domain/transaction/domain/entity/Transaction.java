package com.zkrypto.zkmpc_api.domain.transaction.domain.entity;


import com.zkrypto.zkmpc_api.domain.group.domain.entity.Group;
import com.zkrypto.zkmpc_api.domain.transaction.domain.constant.TransactionStatus;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transactionId", unique = true, nullable = false, length = 64)
    private String transactionId; // 외부용 거래 요청 ID (VARCHAR(64))

    @Column(name = "sender", nullable = false, length = 64)
    private String sender; // 보내는 사람의 지갑 주소

    @Column(name = "receiver", nullable = false, length = 64)
    private String receiver; // 받는 사람의 지갑 주소

    @Column(name = "value", nullable = false)
    private Double value; // 금액

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransactionStatus status;

    @Column(name = "txId", length = 64)
    private String txId; // 트랜잭션 해시 (서명 완료 후)

    @Column(name = "fee")
    private Double fee; // 수수료 (VAT)

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    protected Transaction() {}

    // 생성자 (거래 요청 시 사용)
    public Transaction(String transactionId, String from, String to, Double value, Group group) {
        this.transactionId = transactionId;
        this.sender = from;
        this.receiver = to;
        this.value = value;
        this.status = TransactionStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.group = group;
    }

    // 도메인 비즈니스 로직: 거래 상태 변경 (PATCH /v1/transaction)
    public void updateStatus(TransactionStatus newStatus, String txId, Double fee) {
        if (this.status == TransactionStatus.COMPLETED || this.status == TransactionStatus.FAILED) {
            throw new IllegalStateException("이미 최종 상태인 거래는 변경할 수 없습니다.");
        }

        this.status = newStatus;
        this.txId = txId;
        this.fee = fee;
    }
}
