package com.zkrypto.zkmpc_api.domain.transaction.application.service;

import com.zkrypto.zkmpc_api.domain.enterprise.domain.repository.EnterpriseRepository;
import com.zkrypto.zkmpc_api.domain.group.application.service.GroupService;
import com.zkrypto.zkmpc_api.domain.group.domain.entity.Group;
import com.zkrypto.zkmpc_api.domain.member.domain.repository.MemberRepository;
import com.zkrypto.zkmpc_api.domain.transaction.application.dto.TransactionRequest;
import com.zkrypto.zkmpc_api.domain.transaction.domain.repository.TransactionRepository;
import com.zkrypto.zkmpc_api.infrastructure.ZkMpcClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthAccounts;
import org.web3j.utils.Convert;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        // Hardhat 연결 설정 (Hardhat 노드는 8545에서 실행 중이어야 함)
        "web3j.client-address=http://localhost:8545",
        "ethereum.chain-id=31337",

        // zkMPC 클라이언트 설정 (코어 서버는 8083에서 실행 중이어야 함)
        "zkmpc.core-server-ip=http://localhost:8083" // 💡 실제 IP 주입
})
class TransactionServiceIntegrationTest {

    @Autowired
    private Web3j web3j;

    @Autowired
    private TransactionService transactionService;

    // 💡 zkMpcClient를 MockBean으로 선언하지 않아 실제 구현체가 주입됩니다.
    // @Autowired
    // private ZkMpcClient zkMpcClient; // 실제 클라이언트가 주입됩니다.

    // Context 로드를 위해 필수적인 나머지 의존성들은 Mock 처리합니다.
    @MockBean
    private TransactionRepository transactionRepository;

    @MockBean
    private EnterpriseRepository enterpriseRepository;

    @MockBean
    private MemberRepository memberRepository;

    @MockBean
    private GroupService groupService; // GroupService의 로직은 Mocking 유지합니다.

    // 💡 zkMpcClient는 MockBean에서 제외하고 실제 클라이언트가 로드되도록 합니다.

    private String hardhatTestAccount1; // Sender 계정
    private String hardhatTestAccount2; // Receiver 계정
    private final String TEST_GROUP_ID = "test-group-id";

    @BeforeEach
    void setUp() throws Exception {
        // 1. Hardhat 노드에서 계정 목록을 동적으로 가져오기
        EthAccounts ethAccounts = web3j.ethAccounts().send();
        List<String> accounts = ethAccounts.getAccounts();

        if (accounts.size() < 2) {
            throw new IllegalStateException("Hardhat 노드가 실행 중이 아니거나, 테스트에 필요한 계정(2개 이상)을 제공하지 않았습니다.");
        }

        hardhatTestAccount1 = accounts.get(0);
        hardhatTestAccount2 = accounts.get(1);

        // 2. Group Mock 객체 생성 및 필요한 값 설정
        Group mockGroup = mock(Group.class);
        when(mockGroup.getGroupId()).thenReturn(TEST_GROUP_ID);
        when(mockGroup.getThreshold()).thenReturn(2);
        when(mockGroup.getEnterpriseIds()).thenReturn(Set.of("party1", "party2"));

        // 3. GroupService Mock 설정
        when(groupService.getGroupByAddress(hardhatTestAccount1)).thenReturn(mockGroup);
        when(groupService.getMemberIdByGroupId(TEST_GROUP_ID)).thenReturn("user-member-id");

    }

    @Test
    @DisplayName("Hardhat 노드 연결 및 Nonce 불변성 검증 (트랜잭션 미전송 확인)")
    void checkHardhatConnectionAndNonce() throws Exception {
        // GIVEN: Hardhat 계정 1의 초기 Nonce 값 확인 (실제 통신)
        BigInteger initialNonce = web3j.ethGetTransactionCount(
                hardhatTestAccount1, DefaultBlockParameterName.LATEST
        ).send().getTransactionCount();

        TransactionRequest request = new TransactionRequest(
                hardhatTestAccount1,
                hardhatTestAccount2,
                0.0001
        );

        // WHEN: 거래 요청 (실제 zkMpcClient 호출이 발생하며, 예외 없이 완료되어야 합니다.)
        transactionService.requestTransaction(request);

        // THEN:
        // 1. 최종 Nonce 값 확인 (트랜잭션을 전송하지 않았으므로 Nonce는 증가하지 않아야 함)
        BigInteger finalNonce = web3j.ethGetTransactionCount(
                hardhatTestAccount1, DefaultBlockParameterName.LATEST
        ).send().getTransactionCount();

        // Nonce가 증가하지 않고 초기값과 동일한지 확인
        assertEquals(initialNonce, finalNonce,
                "트랜잭션이 브로드캐스트되지 않았으므로 Nonce 값은 초기값과 동일해야 합니다.");

        // 2. DB 저장 로직만 검증합니다.
        verify(transactionRepository, times(1)).save(any());

        // 💡 zkMpcClient는 실제 통신 성공으로 검증을 대체했습니다.
    }

    @Test
    @DisplayName("zkMPC 코어 서버와 실제 통신 성공 검증")
    void checkRealZkMpcClientConnection() throws Exception {
        // GIVEN
        TransactionRequest request = new TransactionRequest(
                hardhatTestAccount1,
                hardhatTestAccount2,
                1.0
        );

        // GroupService Mocking은 setUp에서 완료됨.

        // WHEN & THEN
        assertDoesNotThrow(() -> {
            transactionService.requestTransaction(request);
        }, "zkMPC 코어 서버와의 실제 통신에 성공해야 합니다. (네트워크 문제/서버 실행 여부 확인 필요)");

        // DB 저장 로직만 검증합니다.
        verify(transactionRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Hardhat 계정의 초기 잔액 확인")
    void checkHardhatAccountBalance() throws Exception {
        // GIVEN
        EthGetBalance balanceResponse = web3j.ethGetBalance(
                hardhatTestAccount1, DefaultBlockParameterName.LATEST
        ).send();

        // WHEN
        BigInteger balanceWei = balanceResponse.getBalance();
        BigInteger expectedWei = Convert.toWei("10000", Convert.Unit.ETHER).toBigInteger();

        // THEN: Hardhat 계정은 기본적으로 10000 ETH를 가지고 시작합니다.
        assertTrue(balanceWei.compareTo(expectedWei) <= 0,
                "계정 잔액이 10000 ETH와 같거나 이전 테스트에서 가스비가 소모된 만큼 작아야 합니다.");
    }
}