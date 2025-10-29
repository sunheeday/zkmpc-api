package com.zkrypto.zkmpc_api.domain.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    @Column(name = "address", unique = true, nullable = false, length = 64)
    private String address; // 지갑 주소

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Member(String memberId, String address) {
        this.memberId = memberId;
        this.address = address;
        this.createdAt = LocalDateTime.now();
    }
}
