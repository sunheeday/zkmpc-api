package com.zkrypto.zkmpc_api.domain.transaction.application.service;


import com.zkrypto.zkmpc_api.domain.group.application.service.GroupService;
import com.zkrypto.zkmpc_api.domain.transaction.application.dto.TransactionListResponse;
import com.zkrypto.zkmpc_api.domain.transaction.application.dto.TransactionRequest;
import com.zkrypto.zkmpc_api.domain.transaction.application.dto.TransactionResponse;
import com.zkrypto.zkmpc_api.domain.transaction.application.dto.TransactionStatusUpdateRequest;
import com.zkrypto.zkmpc_api.domain.transaction.domain.constant.TransactionStatus;
import com.zkrypto.zkmpc_api.domain.transaction.domain.entity.Transaction;
import com.zkrypto.zkmpc_api.domain.transaction.domain.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final GroupService groupService;

    public TransactionService(TransactionRepository transactionRepository, GroupService groupService) {
        this.transactionRepository = transactionRepository;
        this.groupService = groupService;
    }

    // 1. 거래 요청 및 SIGNING 프로토콜 시작 (POST /v1/transaction)
    @Transactional
    public TransactionResponse requestTransaction(TransactionRequest request) {
        String newTransactionId = UUID.randomUUID().toString();

        // 1.1. 거래 엔티티 생성 및 저장 (PENDING 상태)
        Transaction transaction = new Transaction(
                newTransactionId,
                request.getFrom(),
                request.getTo(),
                request.getValue()
        );
        transactionRepository.save(transaction);

        byte[] messageToSign = newTransactionId.getBytes();

        // 1.2. zkMPC SIGNING 프로토콜 시작
        try {
            groupService.startZkMpcProtocol("SIGNING", "그룹ID", null, 0, messageToSign);
        } catch (Exception e) {
            throw new RuntimeException("SIGNING 프로토콜 시작 실패", e);
        }

        return new TransactionResponse(transaction); // 생성된 엔티티 기반 응답
    }

    // 2. 거래 상태 변경 (PATCH /v1/transaction)
    @Transactional
    public void updateTransactionStatus(TransactionStatusUpdateRequest request) {
        Transaction transaction = transactionRepository.findByTransactionId(request.getTransactionId())
                .orElseThrow(() -> new IllegalArgumentException("거래를 찾을 수 없습니다: " + request.getTransactionId()));

        transaction.updateStatus(TransactionStatus.COMPLETED, request.getTxId(), request.getVat());

        transactionRepository.save(transaction);
    }

    // 3. 단일 거래 조회 (GET /v1/transaction)
    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(String transactionId) {
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("거래를 찾을 수 없습니다: " + transactionId));

        // 도메인 엔티티를 응용 프로그램 계층에서 DTO로 변환
        return new TransactionResponse(transaction);
    }

    // 4. 모든 거래 조회 로직 (GET /v1/transaction)
    @Transactional(readOnly = true)
    public TransactionListResponse getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(TransactionResponse::new)
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        TransactionListResponse::new
                ));
    }
}