package com.zkrypto.zkmpc_api.domain.mpc.application.dto;

public record RoundResult(
        String mpcId,
        int roundNum,
        String resultData
) {
}
