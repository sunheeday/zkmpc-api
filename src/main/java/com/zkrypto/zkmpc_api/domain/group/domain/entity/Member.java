package com.zkrypto.zkmpc_api.domain.group.domain.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Embeddable
public class Member {
    private String memberId; // 멤버의 ID
    private String address; // 멤버의 지갑 주소

    public Member(String memberId) {
        this.memberId = memberId;
    }
}
