package com.zkrypto.zkmpc_api.domain.member.application.dto;

import lombok.Getter;

@Getter
public class MemberIdResponse {
    private final String memberId;
    public MemberIdResponse(String memberId){
        this.memberId = memberId;
    }
}
