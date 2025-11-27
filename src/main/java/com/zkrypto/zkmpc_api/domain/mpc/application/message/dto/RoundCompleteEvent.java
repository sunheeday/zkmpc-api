package com.zkrypto.zkmpc_api.domain.mpc.application.message.dto;

import lombok.Builder;

@Builder
public record RoundCompleteEvent(
        String type,
        String roundName,
        String sid
) {
}
