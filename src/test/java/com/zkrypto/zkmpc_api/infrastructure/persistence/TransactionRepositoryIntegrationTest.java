package com.zkrypto.zkmpc_api.infrastructure.persistence;

import com.zkrypto.zkmpc_api.domain.enterprise.domain.entity.Enterprise;
import com.zkrypto.zkmpc_api.domain.group.domain.entity.Group;
import com.zkrypto.zkmpc_api.domain.transaction.domain.constant.TransactionStatus;
import com.zkrypto.zkmpc_api.domain.transaction.domain.entity.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.zkrypto.zkmpc_api.domain.transaction.domain.constant.TransactionStatus.COMPLETED;
import static com.zkrypto.zkmpc_api.domain.transaction.domain.constant.TransactionStatus.FAILED;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnableJpaRepositories(basePackages = "com.zkrypto.zkmpc_api.infrastructure.persistence")
class TransactionRepositoryIntegrationTest {

    @Autowired
    private JpaTransactionRepository jpaTransactionRepository;

    @Autowired
    private JpaGroupRepository jpaGroupRepository;

    @Autowired
    private JpaEnterpriseRepository jpaEnterpriseRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Group group;

    @BeforeEach
    void setUp() {
        jpaTransactionRepository.deleteAll();
        jpaGroupRepository.deleteAll();
        jpaEnterpriseRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        Enterprise enterprise1 = new Enterprise("entId1", "Enterprise Name 1");
        Enterprise enterprise2 = new Enterprise("entId2", "Enterprise Name 2");
        jpaEnterpriseRepository.save(enterprise1);
        jpaEnterpriseRepository.save(enterprise2);

        Set<Enterprise> enterprises = new HashSet<>(Arrays.asList(enterprise1, enterprise2));
        group = new Group("groupId1", enterprises, 2);
        jpaGroupRepository.save(group);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("트랜잭션 저장 및 ID로 조회 성공")
    void saveAndFindByTransactionId_success() {
        // Given
        Transaction transaction = new Transaction("txId1", "sender1", "receiver1", 100.0, group);

        // When
        Transaction savedTransaction = jpaTransactionRepository.save(transaction);
        entityManager.flush();
        entityManager.clear();

        Optional<Transaction> foundTransactionOptional = jpaTransactionRepository.findByTransactionId("txId1");

        // Then
        assertThat(foundTransactionOptional).isPresent();
        Transaction foundTransaction = foundTransactionOptional.get();
        assertThat(foundTransaction.getTransactionId()).isEqualTo("txId1");
        assertThat(foundTransaction.getGroup().getGroupId()).isEqualTo("groupId1");
        assertThat(foundTransaction.getSender()).isEqualTo("sender1");
        assertThat(foundTransaction.getReceiver()).isEqualTo("receiver1");
        assertThat(foundTransaction.getValue()).isEqualTo(100.0);
        assertThat(foundTransaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
    }

    @Test
    @DisplayName("존재하지 않는 트랜잭션 ID 조회 시 Optional.empty 반환")
    void findByTransactionId_notFound_returnsEmptyOptional() {
        // When
        Optional<Transaction> foundTransaction = jpaTransactionRepository.findByTransactionId("nonExistentTxId");

        // Then
        assertThat(foundTransaction).isEmpty();
    }

    @Test
    @DisplayName("모든 트랜잭션 조회 성공")
    void findAll_success() {
        // Given
        Transaction transaction1 = new Transaction("txId1", "sender1", "receiver1", 100.0, group);
        Transaction transaction2 = new Transaction("txId2", "sender2", "receiver2", 200.0, group);
        jpaTransactionRepository.save(transaction1);
        jpaTransactionRepository.save(transaction2);

        transaction1.updateStatus(COMPLETED,"0x78ab12cdef34567890abcdef", 0.000021);
        transaction2.updateStatus(FAILED, null, null);

        entityManager.flush();
        entityManager.clear();

        // When
        List<Transaction> transactions = jpaTransactionRepository.findAll();

        // Then
        assertThat(transactions).hasSize(2);
        assertThat(transactions).extracting(Transaction::getTransactionId)
                .containsExactlyInAnyOrder("txId1", "txId2");
    }
}
