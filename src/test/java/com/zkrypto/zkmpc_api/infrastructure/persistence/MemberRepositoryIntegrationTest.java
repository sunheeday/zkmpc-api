package com.zkrypto.zkmpc_api.infrastructure.persistence;

import com.zkrypto.zkmpc_api.domain.enterprise.domain.entity.Enterprise;
import com.zkrypto.zkmpc_api.domain.group.domain.entity.Group;
import com.zkrypto.zkmpc_api.domain.member.domain.entity.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnableJpaRepositories(basePackages = "com.zkrypto.zkmpc_api.infrastructure.persistence")
class MemberRepositoryIntegrationTest {

    @Autowired
    private JpaMemberRepository jpaMemberRepository;

    @Autowired
    private JpaGroupRepository jpaGroupRepository;

    @Autowired
    private JpaEnterpriseRepository jpaEnterpriseRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Enterprise enterprise1;
    private Enterprise enterprise2;
    private Group group;

    @BeforeEach
    void setUp() {
        // Clear repositories before each test
        jpaMemberRepository.deleteAll();
        jpaGroupRepository.deleteAll();
        jpaEnterpriseRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        enterprise1 = new Enterprise("entId1", "Enterprise Name 1");
        enterprise2 = new Enterprise("entId2", "Enterprise Name 2");
        jpaEnterpriseRepository.save(enterprise1);
        jpaEnterpriseRepository.save(enterprise2);

        Set<Enterprise> enterprises = new HashSet<>(Arrays.asList(enterprise1, enterprise2));
        group = new Group("groupId1", enterprises, 2);
        jpaGroupRepository.save(group);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("멤버 저장 및 ID로 조회 성공")
    void saveAndFindByMemberId_success() {
        // Given
        Member member = new Member("memberId1", "test@example.com");
        member.setGroup(group);

        // When
        Member savedMember = jpaMemberRepository.save(member);
        entityManager.flush();
        entityManager.clear();

        Optional<Member> foundMemberOptional = jpaMemberRepository.findByMemberId("memberId1");

        // Then
        assertThat(foundMemberOptional).isPresent();
        Member foundMember = foundMemberOptional.get();
        assertThat(foundMember.getMemberId()).isEqualTo("memberId1");
        assertThat(foundMember.getEmail()).isEqualTo("test@example.com");
        assertThat(foundMember.getGroup().getGroupId()).isEqualTo("groupId1");
    }

    @Test
    @DisplayName("존재하지 않는 멤버 ID 조회 시 Optional.empty 반환")
    void findByMemberId_notFound_returnsEmptyOptional() {
        // When
        Optional<Member> foundMember = jpaMemberRepository.findByMemberId("nonExistentMemberId");

        // Then
        assertThat(foundMember).isEmpty();
    }

    @Test
    @DisplayName("이메일로 멤버 조회 성공")
    void findByEmail_success() {
        // Given
        Member member = new Member("memberId2", "email@example.com");
        member.setGroup(group);

        jpaMemberRepository.save(member);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<Member> foundMemberOptional = jpaMemberRepository.findByEmail("email@example.com");

        // Then
        assertThat(foundMemberOptional).isPresent();
        assertThat(foundMemberOptional.get().getMemberId()).isEqualTo("memberId2");
    }

    @Test
    @DisplayName("그룹 ID로 멤버 조회 성공")
    void findByGroup_GroupId_success() {
        // Given
        Member member = new Member("memberId4", "group@example.com");
        member.setGroup(group);

        jpaMemberRepository.save(member);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<Member> foundMemberOptional = jpaMemberRepository.findByGroup_GroupId("groupId1");

        // Then
        assertThat(foundMemberOptional).isPresent();
        assertThat(foundMemberOptional.get().getMemberId()).isEqualTo("memberId4");
    }
}
