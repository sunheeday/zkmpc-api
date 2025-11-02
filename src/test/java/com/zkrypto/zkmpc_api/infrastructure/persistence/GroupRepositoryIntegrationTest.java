package com.zkrypto.zkmpc_api.infrastructure.persistence;

import com.zkrypto.zkmpc_api.domain.enterprise.domain.entity.Enterprise;
import com.zkrypto.zkmpc_api.domain.group.domain.entity.Group;
import com.zkrypto.zkmpc_api.domain.group.domain.entity.GroupEnterprise;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@EnableJpaRepositories(basePackages = "com.zkrypto.zkmpc_api.infrastructure.persistence")
class GroupRepositoryIntegrationTest {

    @Autowired
    private JpaGroupRepository jpaGroupRepository;

    @Autowired
    private JpaEnterpriseRepository jpaEnterpriseRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Enterprise enterprise1;
    private Enterprise enterprise2;

    @BeforeEach
    void setUp() {
        enterprise1 = new Enterprise("repoEnterpriseId1", "Repo Enterprise Name 1");
        enterprise2 = new Enterprise("repoEnterpriseId2", "Repo Enterprise Name 2");
        jpaEnterpriseRepository.save(enterprise1);
        jpaEnterpriseRepository.save(enterprise2);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("그룹 저장 및 조회 성공")
    void saveAndFindByGroupId_success() {
        // Given
        Set<Enterprise> enterprises = new HashSet<>(Arrays.asList(enterprise1, enterprise2));
        Group group = new Group("repoGroupId1", enterprises, 2);

        // When
        Group savedGroup = jpaGroupRepository.save(group);
        entityManager.flush();
        entityManager.clear();

        Optional<Group> foundGroupOptional = jpaGroupRepository.findByGroupId("repoGroupId1");

        // Then
        assertThat(foundGroupOptional).isPresent();
        Group foundGroup = foundGroupOptional.get();
        assertThat(foundGroup.getGroupId()).isEqualTo("repoGroupId1");
        assertThat(foundGroup.getThreshold()).isEqualTo(2);
        assertThat(foundGroup.getGroupEnterprises()).hasSize(2);

        Set<String> foundEnterpriseIds = foundGroup.getGroupEnterprises().stream()
                .map(GroupEnterprise::getEnterpriseId)
                .collect(Collectors.toSet());
        assertThat(foundEnterpriseIds).containsExactlyInAnyOrder("repoEnterpriseId1", "repoEnterpriseId2");
    }

    @Test
    @DisplayName("존재하지 않는 그룹 ID 조회 시 Optional.empty 반환")
    void findByGroupId_notFound_returnsEmptyOptional() {
        // When
        Optional<Group> foundGroup = jpaGroupRepository.findByGroupId("nonExistentGroupId");

        // Then
        assertThat(foundGroup).isEmpty();
    }

    @Test
    @DisplayName("그룹 생성 시 2개 미만의 엔터프라이즈 ID로 예외 발생")
    void groupCreation_lessThanTwoEnterprises_throwsException() {
        // Given
        Set<Enterprise> singleEnterprise = new HashSet<>(Arrays.asList(enterprise1));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new Group("invalidGroupId", singleEnterprise, 1);
        });

        assertThat(exception.getMessage()).contains("그룹 등록을 위해서는 최소 2개 이상의 엔터프라이즈 ID가 필요합니다.");
    }
}
