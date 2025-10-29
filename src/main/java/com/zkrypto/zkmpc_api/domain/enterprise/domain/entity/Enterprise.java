package com.zkrypto.zkmpc_api.domain.enterprise.domain.entity;

import com.zkrypto.zkmpc_api.domain.group.domain.entity.Group;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Table(name = "enterprise")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Enterprise {

    @Id
    @Column(name = "enterprise_id", unique = true, nullable = false, length = 64)
    private String enterpriseId; // PK

    @Column(name = "name", unique = true, nullable = false, length = 64)
    private String name;


    @ManyToMany(mappedBy = "enterprises")
    private Set<Group> groups = new HashSet<>();

    public Enterprise(String enterpriseId, String name) {
        // 도메인 규칙 검증
        if (enterpriseId == null || enterpriseId.isEmpty()) {
            throw new IllegalArgumentException("Enterprise ID는 필수입니다.");
        }
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Enterprise 이름은 필수입니다.");
        }
        this.enterpriseId = enterpriseId;
        this.name = name;
    }

}