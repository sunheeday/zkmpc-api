package com.zkrypto.zkmpc_api.domain.member.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberRegisterRequest {

    @NotBlank
    private String email;
    @NotEmpty
    private String authCode;
    @NotBlank
    private String address;

}
