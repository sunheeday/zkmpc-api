package com.zkrypto.zkmpc_api.infrastructure.persistence;

import com.zkrypto.zkmpc_api.domain.enterprise.domain.entity.Enterprise;
import com.zkrypto.zkmpc_api.domain.enterprise.domain.repository.EnterpriseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EnterpriseRepositoryIntegrationTest {

    @Autowired
    private JpaEnterpriseRepository jpaEnterpriseRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        jpaEnterpriseRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("엔터프라이즈 저장 및 ID로 조회 성공")
    void saveAndFindByEnterpriseId_success() {
        // Given
        Enterprise enterprise = new Enterprise("entId1", "Enterprise Name 1");

        // When
        Enterprise savedEnterprise = jpaEnterpriseRepository.save(enterprise);
        entityManager.flush();
        entityManager.clear();

        Optional<Enterprise> foundEnterpriseOptional = jpaEnterpriseRepository.findByEnterpriseId("entId1");

        // Then
        assertThat(foundEnterpriseOptional).isPresent();
        Enterprise foundEnterprise = foundEnterpriseOptional.get();
        assertThat(foundEnterprise.getEnterpriseId()).isEqualTo("entId1");
        assertThat(foundEnterprise.getName()).isEqualTo("Enterprise Name 1");
    }

    @Test
    @DisplayName("존재하지 않는 엔터프라이즈 ID 조회 시 Optional.empty 반환")
    void findByEnterpriseId_notFound_returnsEmptyOptional() {
        // When
        Optional<Enterprise> foundEnterprise = jpaEnterpriseRepository.findByEnterpriseId("nonExistentEntId");

        // Then
        assertThat(foundEnterprise).isEmpty();
    }

    @Test
    @DisplayName("모든 엔터프라이즈 조회 성공")
    void findAll_success() {
        // Given
        Enterprise enterprise1 = new Enterprise("entId1", "Enterprise Name 1");
        Enterprise enterprise2 = new Enterprise("entId2", "Enterprise Name 2");
        jpaEnterpriseRepository.save(enterprise1);
        jpaEnterpriseRepository.save(enterprise2);
        entityManager.flush();
        entityManager.clear();

        // When
        List<Enterprise> enterprises = jpaEnterpriseRepository.findAll();

        // Then
        assertThat(enterprises).hasSize(2);
        assertThat(enterprises).extracting(Enterprise::getEnterpriseId)
                .containsExactlyInAnyOrder("entId1", "entId2");
    }

    @Test
    @DisplayName("엔터프라이즈 ID 존재 여부 확인 성공")
    void existsByEnterpriseId_success() {
        // Given
        Enterprise enterprise = new Enterprise("entId1", "Enterprise Name 1");
        jpaEnterpriseRepository.save(enterprise);
        entityManager.flush();
        entityManager.clear();

        // When
        boolean exists = jpaEnterpriseRepository.existByEnterpriseId("entId1");
        boolean notExists = jpaEnterpriseRepository.existByEnterpriseId("nonExistentEntId");

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
}
