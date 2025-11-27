package com.zkrypto.zkmpc_api.domain.mpc.application.message.dto;

import com.zkrypto.constant.ParticipantType;
import lombok.Builder;

@Builder
public record ProtocolCompleteEvent(
        String sid,
        String memberId,
        ParticipantType type
) {
}
