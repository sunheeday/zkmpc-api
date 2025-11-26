package com.zkrypto.zkmpc_api.domain.member.domain.entity;

import com.zkrypto.zkmpc_api.domain.group.domain.entity.Group;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @Column(name = "member_id", unique = true, nullable = false)
    private String memberId; // PK

    @Column(name = "address", unique = true, length = 64)
    private String address; // 지갑 주소

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id") // member 테이블의 groupid 칼럼을 외래 키로 사용
    private Group group; // Group 객체를 참조

    public Member(String memberId, String email) {
        this.memberId = memberId;
        this.address = null;
        this.email = email;
        this.createdAt = LocalDateTime.now();
        this.group = null;
    }

    public void setGroup(Group group) {
        if (this.group != null) {
            throw new IllegalStateException("멤버 [" + this.memberId + "]는 이미 그룹에 속해 있습니다.");
        }
        this.group = group;
    }
}
