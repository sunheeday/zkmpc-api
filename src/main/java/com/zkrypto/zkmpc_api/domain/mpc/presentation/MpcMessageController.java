package com.zkrypto.zkmpc_api.domain.mpc.presentation;

import com.zkrypto.zkmpc_api.domain.mpc.application.dto.RoundResult;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class MpcMessageController {

    private final RabbitTemplate rabbitTemplate;

    // 클라이언트가 라운드 처리 결과 STOMP SEND를 보낼 때
    @MessageMapping("/mpc/result")
    public void handleRoundResult(@Payload RoundResult result) {

        // Core 서버의 메시지 수신 큐(예: core.mpc.input.queue)로 라우팅
        rabbitTemplate.convertAndSend(
                "tss.exchange",
                "mpc.round.input." + result.mpcId(), // 라우팅 키
                result // DTO 페이로드
        );
    }
}