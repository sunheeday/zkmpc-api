package com.zkrypto.zkmpc_api.domain.group.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ZkMpcStartRequest {
    @NotBlank
    private String process; // 실행할 프로토콜 (KEY_GENERATION, SIGNING, REFRESH)

    @NotBlank
    private String sid; // 그룹 id

    @NotNull
    private List<String> memberIds; // 해당 프로토콜에 참여할 멤버 목록

    @NotNull
    private Integer threshold; // 해당 그룹의 threshold

    private byte[] messageBytes; // SIGNING 시 사용할 메시지
}
