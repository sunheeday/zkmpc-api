package com.zkrypto.zkmpc_api.infrastructure.external;

import com.zkrypto.zkmpc_api.domain.mpc.application.dto.MpcStatusUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MpcStatusPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void publishMpcStatus(MpcStatusUpdate update) {
        String destination = "/topic/" + update.mpcId();
        messagingTemplate.convertAndSend(destination, update);
    }
}