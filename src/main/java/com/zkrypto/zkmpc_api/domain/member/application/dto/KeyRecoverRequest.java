package com.zkrypto.zkmpc_api.domain.member.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class KeyRecoverRequest {
    @NotBlank
    private String memberId; // 등록할 멤버의 ID
}
