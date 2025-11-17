package com.zkrypto.zkmpc_api.domain.mpc.application.dto;

public record MpcStatusUpdate(
        String mpcId,
        String statusMessage,
        int roundNum
) {
}
