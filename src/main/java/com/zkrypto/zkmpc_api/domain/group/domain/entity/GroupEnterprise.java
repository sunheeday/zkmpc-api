package com.zkrypto.zkmpc_api.domain.group.domain.entity;

import com.zkrypto.zkmpc_api.domain.enterprise.domain.entity.Enterprise;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name="group_enterprise")
public class GroupEnterprise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enterprise_id", nullable = false)
    private Enterprise enterprise;

    public GroupEnterprise(Group group, Enterprise enterprise) {
        this.group = group;
        this.enterprise = enterprise;
    }

    public String getEnterpriseId(){
        return this.enterprise.getEnterpriseId();
    }

}
