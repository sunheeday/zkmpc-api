package com.zkrypto.zkmpc_api.domain.member.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AddressRegisterRequest {

    @NotNull
    private String memberId;
    @NotNull
    private String address;
}
