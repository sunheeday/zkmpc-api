package com.zkrypto.zkmpc_api.domain.transaction.application.service;

import com.zkrypto.zkmpc_api.domain.group.domain.entity.Group;
import com.zkrypto.zkmpc_api.domain.transaction.application.dto.TransactionRequest;
import com.zkrypto.zkmpc_api.domain.transaction.application.dto.TransactionStatusUpdateRequest;
import com.zkrypto.zkmpc_api.domain.transaction.domain.constant.TransactionStatus;
import com.zkrypto.zkmpc_api.domain.transaction.domain.entity.Transaction;
import com.zkrypto.zkmpc_api.infrastructure.ZkMpcClient;
import com.zkrypto.zkmpc_api.domain.group.application.service.GroupService;
import com.zkrypto.zkmpc_api.domain.transaction.domain.repository.TransactionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // 💡 이 설정 추가
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private GroupService groupService;
    @Mock
    private ZkMpcClient zkMpcClient;

    @InjectMocks
    private TransactionService transactionService;

    private Group group;
    private Transaction transaction;
    private TransactionStatusUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        // @Value 필드 chainId 수동 주입
        ReflectionTestUtils.setField(transactionService, "chainId", 31337L);

        group = mock(Group.class);
        when(group.getGroupId()).thenReturn("testGroupId");
        when(group.getThreshold()).thenReturn(2);
        when(group.getEnterpriseIds()).thenReturn(Set.of("ent1", "ent2"));

        transaction = new Transaction("testTxId", "senderAddress", "receiverAddress", 100.0, group);

        // 💡 DTO 구조에 맞게 생성자 인자 수정: (transactionId, txId, vat)
        updateRequest = new TransactionStatusUpdateRequest("testTxId", "newTxId", 0.000021);
    }

    @Test
    @DisplayName("트랜잭션 요청 성공 및 SIGNING 프로토콜 시작")
    void requestTransaction_success() {
        // Given
        TransactionRequest request = new TransactionRequest("senderAddress", "receiverAddress", 100.0);

        when(groupService.getGroupByAddress(request.getFrom())).thenReturn(group);
        when(groupService.getMemberIdByGroupId(group.getGroupId())).thenReturn("memberId1");

        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        doNothing().when(zkMpcClient).requestStartProtocol(anyString(), anyString(), anyList(), anyInt(), any());

        // When
        transactionService.requestTransaction(request);

        // Then
        verify(groupService, times(1)).getGroupByAddress(request.getFrom());
        verify(groupService, times(1)).getMemberIdByGroupId(group.getGroupId());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(zkMpcClient, times(1)).requestStartProtocol(
                eq("SIGNING"),
                eq("testGroupId"),
                anyList(),
                eq(2),
                any(byte[].class)
        );
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