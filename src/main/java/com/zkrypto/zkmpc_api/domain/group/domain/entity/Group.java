package com.zkrypto.zkmpc_api.domain.group.domain.entity;

import com.zkrypto.zkmpc_api.domain.enterprise.domain.entity.Enterprise;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 내부 PK

    @Column(name = "group_id", unique = true, nullable = false)
    private String groupId; // 외부용 ID (ERD의 groupID)

    @Column(name = "threshold", nullable = false)
    private Integer threshold; // 출금 승인 최소 인원

//    @ElementCollection(fetch = FetchType.EAGER)
//    @CollectionTable(
//            name = "group_enterprise",
//            joinColumns = @JoinColumn(name = "group_id")
//    )
//    @Column(name = "enterprise_id")
//    private Set<String> enterpriseIds = new HashSet<>();
    @OneToMany(mappedBy = "group")
    private Set<GroupEnterprise> groupEnterprises = new HashSet<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Group(String groupId, Set<Enterprise> enterpriseIds, Integer threshold) {
        // 도메인 규칙 검증: enterprise는 최소 2개 이상이어야 함
        if (enterpriseIds == null || enterpriseIds.size() < 2) {
            throw new IllegalArgumentException("그룹 등록을 위해서는 최소 2개 이상의 엔터프라이즈 ID가 필요합니다.");
        }

        this.groupId = groupId;
        this.groupEnterprises = enterpriseIds.stream()
                .map(enterprise -> new GroupEnterprise(this, enterprise))
                .collect(Collectors.toSet());
        this.threshold = threshold;
        this.createdAt = LocalDateTime.now();
    }

    public Set<String> getEnterpriseIds() {
        return this.groupEnterprises.stream()
                .map(GroupEnterprise::getEnterpriseId)
                .collect(Collectors.toSet());
    }

    public Set<Enterprise> getEnterprises() {
        return this.groupEnterprises.stream()
                .map(GroupEnterprise::getEnterprise)
                .collect(Collectors.toSet());
    }
}
