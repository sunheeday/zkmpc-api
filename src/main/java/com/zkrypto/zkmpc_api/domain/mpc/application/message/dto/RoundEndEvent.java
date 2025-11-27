package com.zkrypto.zkmpc_api.domain.mpc.application.message.dto;

import lombok.Builder;

@Builder
public record RoundEndEvent(
        String message,
        String type,
        String sid
) {
}
