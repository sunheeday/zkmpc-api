package com.zkrypto.zkmpc_api.domain.transaction.application.service;


import com.zkrypto.zkmpc_api.domain.group.application.service.GroupService;
import com.zkrypto.zkmpc_api.domain.group.domain.entity.Group;
import com.zkrypto.zkmpc_api.domain.transaction.application.dto.TransactionListResponse;
import com.zkrypto.zkmpc_api.domain.transaction.application.dto.TransactionRequest;
import com.zkrypto.zkmpc_api.domain.transaction.application.dto.TransactionResponse;
import com.zkrypto.zkmpc_api.domain.transaction.application.dto.TransactionStatusUpdateRequest;
import com.zkrypto.zkmpc_api.domain.transaction.domain.constant.TransactionStatus;
import com.zkrypto.zkmpc_api.domain.transaction.domain.entity.Transaction;
import com.zkrypto.zkmpc_api.domain.transaction.domain.repository.TransactionRepository;
import com.zkrypto.zkmpc_api.infrastructure.ZkMpcClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.List;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.Sign;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.utils.Convert;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final GroupService groupService;
    private final ZkMpcClient zkMpcClient;

    @Value("${ethereum.chain-id}")
    private Long chainId;

    public TransactionService(TransactionRepository transactionRepository, GroupService groupService, ZkMpcClient zkMpcClient) {
        this.transactionRepository = transactionRepository;
        this.groupService = groupService;
        this.zkMpcClient = zkMpcClient;

    }

    // 1. 거래 요청 및 SIGNING 프로토콜 시작 (POST /v1/transaction)
    @Transactional
    public TransactionResponse requestTransaction(TransactionRequest request) {
        String newTransactionId = UUID.randomUUID().toString();

        Group group = groupService.getGroupByAddress(request.getFrom());
        String groupId = group.getGroupId();

        // 1.1. 거래 엔티티 생성 및 저장 (PENDING 상태)
        Transaction transaction = new Transaction(
                newTransactionId,
                request.getFrom(),
                request.getTo(),
                request.getValue(),
                group
        );
        transactionRepository.save(transaction);

//        byte[] messageToSign = newTransactionId.getBytes();
        //TODO 그..from to 값을 이용해서 이더리움 트랜잭션 형태로 만들기 && messageToSign을 유저한테 반환해야함

        String senderAddress = transaction.getSender(); //from
        String toAddress = transaction.getReceiver(); //to
        BigInteger nonce = BigInteger.ZERO;
        BigInteger gasPrice = BigInteger.valueOf(20_000_000_000L); // 20 Gwei
        BigInteger gasLimit = BigInteger.valueOf(21_000L);

        BigInteger valueInWei = Convert.toWei(
                String.valueOf(request.getValue()),
                Convert.Unit.ETHER
        ).toBigInteger();

        RawTransaction rawTransaction = RawTransaction
                .createTransaction(nonce, gasPrice, gasLimit, toAddress, valueInWei, "");

        byte[] encodedTxForSigning = TransactionEncoder.encode(
                rawTransaction,
                new Sign.SignatureData(BigInteger.valueOf(this.chainId).toByteArray(),
                        new byte[]{}, new byte[]{}));

        //사용자
        String memberId = groupService.getMemberIdByGroupId(groupId);

        //파티들 + 사용자
        List<String> memberIds = new ArrayList<>(group.getEnterpriseIds());
        memberIds.add(memberId);

        Integer threshold = group.getThreshold();



        try {
            zkMpcClient.requestStart("SIGNING", groupId, memberIds, threshold, encodedTxForSigning);
        } catch (Exception e) {
            throw new RuntimeException("SIGNING 프로토콜 시작 실패", e);
        }

        return new TransactionResponse(transaction);
    }

    // 2. 거래 상태 변경 (PATCH /v1/transaction)
    @Transactional
    public void updateTransactionStatus(TransactionStatusUpdateRequest request) {
        Transaction transaction = transactionRepository.findByTransactionId(request.getTransactionId())
                .orElseThrow(() -> new IllegalArgumentException("거래를 찾을 수 없습니다: " + request.getTransactionId()));

        transaction.updateStatus(TransactionStatus.COMPLETED, request.getTxId(), request.getVat());
    }

    // 3. 단일 거래 조회 (GET /v1/transaction)
    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(String transactionId) {
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("거래를 찾을 수 없습니다: " + transactionId));

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