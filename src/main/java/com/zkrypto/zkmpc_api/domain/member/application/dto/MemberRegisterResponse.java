package com.zkrypto.zkmpc_api.domain.member.application.dto;

import lombok.Getter;

@Getter
public class MemberRegisterResponse {
    private final String memberId;
    public MemberRegisterResponse(String memberId){
        this.memberId = memberId;
    }
}
