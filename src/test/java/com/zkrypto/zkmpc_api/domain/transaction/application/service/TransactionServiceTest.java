package com.zkrypto.zkmpc_api.domain.transaction.application.service;

import com.zkrypto.zkmpc_api.domain.group.application.service.GroupService;
import com.zkrypto.zkmpc_api.domain.group.domain.entity.Group;
import com.zkrypto.zkmpc_api.domain.transaction.application.dto.TransactionRequest;
import com.zkrypto.zkmpc_api.domain.transaction.application.dto.TransactionStatusUpdateRequest;
import com.zkrypto.zkmpc_api.domain.transaction.domain.constant.TransactionStatus;
import com.zkrypto.zkmpc_api.domain.transaction.domain.entity.Transaction;
import com.zkrypto.zkmpc_api.domain.transaction.domain.repository.TransactionRepository;
import com.zkrypto.zkmpc_api.infrastructure.ZkMpcClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Optional;
import java.util.Set;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private GroupService groupService;
    @Mock
    private ZkMpcClient zkMpcClient;

    @Mock
    private Web3j web3j;

    @InjectMocks
    private TransactionService transactionService;

    private Group group;
    private Transaction transaction;
    private TransactionStatusUpdateRequest updateRequest;

    private final String TEST_FROM_ADDRESS = "senderAddress";
    private final String TEST_TO_ADDRESS = "receiverAddress";
    private final String TEST_GROUP_ID = "testGroupId";
    private final Long TEST_CHAIN_ID = 31337L; // Hardhat Chain ID

    @BeforeEach
    void setUp() throws Exception {
        ReflectionTestUtils.setField(transactionService, "chainId", TEST_CHAIN_ID);

        group = mock(Group.class);
        when(group.getGroupId()).thenReturn(TEST_GROUP_ID);
        when(group.getThreshold()).thenReturn(2);
        when(group.getEnterpriseIds()).thenReturn(Set.of("ent1", "ent2"));

        transaction = new Transaction("testTxId", TEST_FROM_ADDRESS, TEST_TO_ADDRESS, 100.0, group);

        updateRequest = new TransactionStatusUpdateRequest("testTxId", "newTxId", 0.000021);

        // --- Web3j Nonce 획득 기본 Mock 설정 (성공 케이스를 위한 준비) ---
        EthGetTransactionCount mockNonceResponse = mock(EthGetTransactionCount.class);
        when(mockNonceResponse.getTransactionCount()).thenReturn(BigInteger.ZERO); // 초기 Nonce = 0

        // web3j.ethGetTransactionCount().send() 호출 시 응답 Mocking
        when(web3j.ethGetTransactionCount(eq(TEST_FROM_ADDRESS), eq(DefaultBlockParameterName.LATEST)))
                .thenReturn(mock(org.web3j.protocol.core.Request.class));
        when(web3j.ethGetTransactionCount(eq(TEST_FROM_ADDRESS), eq(DefaultBlockParameterName.LATEST)).send())
                .thenReturn(mockNonceResponse);
    }

    @Test
    @DisplayName("트랜잭션 요청 성공, Web3j Nonce 획득 및 SIGNING 프로토콜 시작")
    void requestTransaction_success() throws Exception {
        // Given
        TransactionRequest request = new TransactionRequest(TEST_FROM_ADDRESS, TEST_TO_ADDRESS, 100.0);

        // MPC 클라이언트로 전달되는 인코딩된 트랜잭션을 캡처하기 위한 캡처 객체
        ArgumentCaptor<byte[]> messageCaptor = ArgumentCaptor.forClass(byte[].class);

        when(groupService.getGroupByAddress(request.getFrom())).thenReturn(group);
        when(groupService.getMemberIdByGroupId(group.getGroupId())).thenReturn("memberId1");
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        doNothing().when(zkMpcClient).requestStartProtocol(anyString(), anyString(), anyList(), anyInt(), any());

        // When
        transactionService.requestTransaction(request);

        // Then
        // 1. Web3j Nonce 획득 확인 (Hardhat 통신 시뮬레이션)
        verify(web3j, times(1)).ethGetTransactionCount(eq(TEST_FROM_ADDRESS), eq(DefaultBlockParameterName.LATEST));

        // 2. 비즈니스 로직 및 저장 확인
        verify(groupService, times(1)).getGroupByAddress(request.getFrom());
        verify(transactionRepository, times(1)).save(any(Transaction.class));

        // 3. ZkMpcClient 호출 확인 및 인코딩된 트랜잭션 메시지 검증
        verify(zkMpcClient, times(1)).requestStartProtocol(
                eq("SIGNING"),
                eq(TEST_GROUP_ID),
                anyList(), // memberIds + enterpriseIds
                eq(2),
                messageCaptor.capture() // 인코딩된 트랜잭션 캡처
        );

        byte[] encodedTx = messageCaptor.getValue();
        assertThat(encodedTx.length).as("인코딩된 트랜잭션 메시지가 비어있지 않아야 합니다.").isGreaterThan(50);
    }

    @Test
    @DisplayName("트랜잭션 생성 실패 - 그룹을 찾을 수 없음")
    void requestTransaction_fail_groupNotFound() {
        // Given
        TransactionRequest request = new TransactionRequest("nonExistentSender", "receiver", 100.0);

        doThrow(new IllegalArgumentException("존재하지 않는 그룹 ID입니다.")).when(groupService).getGroupByAddress(request.getFrom());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.requestTransaction(request);
        });

        assertThat(exception.getMessage()).contains("존재하지 않는 그룹 ID입니다.");
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(zkMpcClient, never()).requestStartProtocol(anyString(), anyString(), anyList(), anyInt(), any());
    }

    @Test
    @DisplayName("트랜잭션 생성 실패 - Nonce 획득 중 네트워크 오류 발생 (Hardhat 통신 실패 시뮬레이션)")
    void requestTransaction_fail_networkErrorOnNonce() throws Exception {
        // Given
        TransactionRequest request = new TransactionRequest(TEST_FROM_ADDRESS, TEST_TO_ADDRESS, 100.0);

        // GroupService Mock 설정 (그룹은 찾아짐)
        Group mockGroup = mock(Group.class);
        when(groupService.getGroupByAddress(TEST_FROM_ADDRESS)).thenReturn(mockGroup);

        // Web3j 호출 시 IOException 발생 Mocking
        when(web3j.ethGetTransactionCount(anyString(), any()).send())
                .thenThrow(new IOException("Timeout"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionService.requestTransaction(request);
        });

        assertThat(exception.getMessage()).contains("논스 값을 가져오는 중 네트워크 오류 발생");

        // 트랜잭션이 저장되거나 MPC 프로토콜이 시작되지 않았는지 확인
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(zkMpcClient, never()).requestStartProtocol(anyString(), anyString(), anyList(), anyInt(), any());
    }

    @Test
    @DisplayName("트랜잭션 상태 업데이트 성공")
    void updateTransactionStatus_success() {
        // Given
        when(transactionRepository.findByTransactionId(anyString())).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // When
        transactionService.updateTransactionStatus(updateRequest);
        transactionRepository.save(transaction);

        // Then
        // 💡 트랜잭션 엔티티의 상태 변경 검증 (COMPLETED 상태는 Service 내부에서 고정)
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(transaction.getTxId()).isEqualTo(updateRequest.getTxId());

        verify(transactionRepository, times(1)).findByTransactionId(updateRequest.getTransactionId());
        verify(transactionRepository, times(1)).save(transaction);
    }

    @Test
    @DisplayName("트랜잭션 상태 업데이트 실패 - 트랜잭션을 찾을 수 없음")
    void updateTransactionStatus_fail_transactionNotFound() {
        // Given
        TransactionStatusUpdateRequest request = new TransactionStatusUpdateRequest("nonExistentTxId", "newTxId", 1.0); // DTO 구조에 맞게 수정
        when(transactionRepository.findByTransactionId(anyString())).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.updateTransactionStatus(request);
        });
        assertThat(exception.getMessage()).contains("거래를 찾을 수 없습니다: " + request.getTransactionId());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("트랜잭션 ID로 단일 트랜잭션 조회 성공")
    void getTransactionById_success() {
        // Given
        when(transactionRepository.findByTransactionId(anyString())).thenReturn(Optional.of(transaction));

        // When
        transactionService.getTransaction("testTxId");

        // Then
        verify(transactionRepository, times(1)).findByTransactionId("testTxId");
    }

    @Test
    @DisplayName("트랜잭션 ID로 단일 트랜잭션 조회 실패 - 트랜잭션을 찾을 수 없음")
    void getTransactionById_fail_transactionNotFound() {
        // Given
        when(transactionRepository.findByTransactionId(anyString())).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.getTransaction("nonExistentTxId");
        });
        assertThat(exception.getMessage()).contains("거래를 찾을 수 없습니다: nonExistentTxId");
        verify(transactionRepository, times(1)).findByTransactionId("nonExistentTxId");
    }
}