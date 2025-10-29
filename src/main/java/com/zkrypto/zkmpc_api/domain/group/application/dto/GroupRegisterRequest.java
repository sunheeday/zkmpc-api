package com.zkrypto.zkmpc_api.domain.group.application.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GroupRegisterRequest {
    @NotBlank
    private String memberId; // 등록할 멤버의 ID

    @NotEmpty
    @Size(min = 2, message = "기업 ID는 최소 2개 이상이어야 합니다.")
    private List<String> enterprises; // 지갑 관리 리스트 (enterpriseId 목록)

    @NotNull
    private Integer threshold; // 출금 승인 최소 인원
}
