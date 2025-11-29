package com.zkrypto.zkmpc_api.domain.transaction.application.service;


import com.zkrypto.zkmpc_api.domain.group.application.service.GroupService;
import com.zkrypto.zkmpc_api.domain.group.domain.entity.Group;
import com.zkrypto.zkmpc_api.domain.group.domain.repository.GroupRepository;
import com.zkrypto.zkmpc_api.domain.member.domain.entity.Member;
import com.zkrypto.zkmpc_api.domain.member.domain.repository.MemberRepository;
import com.zkrypto.zkmpc_api.domain.mpc.application.websocket.WebSocketService;
import com.zkrypto.zkmpc_api.domain.transaction.application.dto.TransactionRequest;
import com.zkrypto.zkmpc_api.domain.transaction.application.dto.TransactionResponse;
import com.zkrypto.zkmpc_api.domain.transaction.application.dto.TransactionStatusUpdateRequest;
import com.zkrypto.zkmpc_api.domain.transaction.domain.constant.TransactionStatus;
import com.zkrypto.zkmpc_api.domain.transaction.domain.entity.Transaction;
import com.zkrypto.zkmpc_api.domain.transaction.domain.repository.TransactionRepository;
import com.zkrypto.zkmpc_api.infrastructure.ZkMpcClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.Sign;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final MemberRepository memberRepository;
    private final GroupRepository groupRepository;

    private final WebSocketService webSocketService;
    private final ZkMpcClient zkMpcClient;
    private final Web3j web3j;

    @Value("${ethereum.chain-id}")
    private Long chainId;
    private String address;


    private BigInteger getNonce(String address) {
        try {
            EthGetTransactionCount ethGetTransactionCount =
                    web3j.ethGetTransactionCount(address, DefaultBlockParameterName.LATEST).send();

            return ethGetTransactionCount.getTransactionCount();

        } catch (IOException e) {
            throw new RuntimeException("논스 값을 가져오는 중 네트워크 오류 발생 (주소: " + address + ")", e);
        }
    }

    // 1. 거래 요청 및 SIGNING 프로토콜 시작 (POST /v1/transaction)
    @Transactional
    public TransactionResponse requestTransaction(TransactionRequest request) {
        String newTransactionId = UUID.randomUUID().toString();

//        Group group = groupService.getGroupByAddress(request.getFrom());

//        Group group = memberRepository.findByAddress(request.getFrom())
//                .orElseThrow(() -> new IllegalArgumentException("해당 지갑 주소의 멤버가 없습니다: " + address))
//                .getGroup();



//        String groupId = group.getGroupId();
//
//        // 1.1. 거래 엔티티 생성 및 저장 (PENDING 상태)
//        Transaction transaction = new Transaction(
//                newTransactionId,
//                request.getFrom(),
//                request.getTo(),
//                request.getValue(),
//                group
//        );
//        transactionRepository.save(transaction);

//        byte[] messageToSign = newTransactionId.getBytes();
        //TODO 그..from to 값을 이용해서 이더리움 트랜잭션 형태로 만들기 && messageToSign을 유저한테 반환해야함

//        String fromAddress = request.getFrom(); //from
//        String toAddress = request.getTo(); //to
//        BigInteger nonce = getNonce(fromAddress);
//
//
//        BigInteger gasPrice = BigInteger.valueOf(20_000_000_000L); // 20 Gwei
//        BigInteger gasLimit = BigInteger.valueOf(21_000L);
//
//        BigInteger valueInWei = Convert.toWei(
//                String.valueOf(request.getValue()),
//                Convert.Unit.ETHER
//        ).toBigInteger();
//
//        RawTransaction rawTransaction = RawTransaction
//                .createTransaction(nonce, gasPrice, gasLimit, toAddress, valueInWei, "0x");

//        byte[] encodedTxForSigning = TransactionEncoder.encode(
//                rawTransaction,
//                new Sign.SignatureData(BigInteger.valueOf(this.chainId).toByteArray(),
//                        new byte[]{}, new byte[]{}));

        //사용자
//        String memberId = groupService.getMemberIdByGroupId(groupId);

//        String memberId = memberRepository.findByGroupGroupId(groupId)
//                .orElseThrow(() -> new IllegalArgumentException("해당 그룹에 존재하는 멤버가 없습니다: " + groupId))
//                .getMemberId();

        //파티들 + 사용자
        List<String> memberIds = new ArrayList<>();
        memberIds.add("2");
        memberIds.add("3");

//        Integer threshold = group.getThreshold();
//


        try {
            zkMpcClient.requestStartProtocol("SIGNING", "1", memberIds, 2,  request.getTx().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("SIGNING 프로토콜 시작 실패", e);
        }

        return null;
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
    public List<TransactionResponse> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(TransactionResponse::new)
                .collect(Collectors.toList());
    }

    public void sign(String message) {
        String[] split = message.split("/");
        String sign = split[0];
        String sid = split[1];

//        Member member = memberRepository.findByGroupGroupId(sid).orElseThrow();
//        webSocketService.deliverMessageToApp(member.getMemberId(), sign);
        webSocketService.deliverMessageToApp("1", sign);
    }
}