package com.zkrypto.zkmpc_api.domain.mpc.presentation;
import com.zkrypto.zkmpc_api.domain.mpc.application.message.dto.InitProtocolEndEvent;
import com.zkrypto.zkmpc_api.domain.mpc.application.message.dto.ProtocolCompleteEvent;
import com.zkrypto.zkmpc_api.domain.mpc.application.message.dto.RoundCompleteEvent;
import com.zkrypto.zkmpc_api.domain.mpc.application.message.dto.RoundEndEvent;
import com.zkrypto.zkmpc_api.infrastructure.amqp.RabbitMqBroker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketMessageController {

    private final RabbitMqBroker rabbitMqBroker;

    // --- Helper 메서드 (요청 DTO를 RabbitMQ 발행용 이벤트 DTO로 변환) ---
//    private RoundCompleteEvent toRoundCompleteEvent(AppRoundResultRequest request) {
//        // ... (구현 필요)
//        return new RoundCompleteEvent(request.getClientId(), /* ... data ... */);
//    }
//    private RoundEndEvent toRoundEndEvent(AppRoundResultRequest request) {
//        // ... (구현 필요)
//        return new RoundEndEvent(request.getClientId(), /* ... data ... */);
//    }
//    private ProtocolCompleteEvent toProtocolCompleteEvent(AppRoundResultRequest request) {
//        // ... (구현 필요)
//        return new ProtocolCompleteEvent(request.getClientId(), /* ... data ... */);
//    }


    /**
     * 0. 초기화 완료 메시지 전송
     * 앱이 초기화 완료 후 '/app/init/end'로 메시지를 보낼 때 호출
     * 라우팅 키: topic.init.end
     */
    @MessageMapping("/init/end")
    public void handleInitEnd(@Payload InitProtocolEndEvent request) {

        log.info("[WS] 앱으로부터 초기화 완료 결과 수신. Client ID: {}", request.memberId());

//        InitProtocolEndEvent event = toInitProtocolEndEvent(request);

        rabbitMqBroker.publish(request);

        log.info("Core 서버로 InitProtocolEndEvent 발행 완료.");
    }


    /**
     * 1. 라운드 중간 완료 메시지 전송
     * 앱이 '/app/round/complete'로 보낸 메시지를 처리
     * 라우팅 키: topic.round.complete
     */
    @MessageMapping("/round/complete")
    public void handleRoundComplete(@Payload RoundCompleteEvent request) {

        log.info("[WS] 라운드 중간 완료 결과 수신. round: {}", request.roundName());

//        RoundCompleteEvent event = toRoundCompleteEvent(request);

        rabbitMqBroker.publish(request);

        log.debug("Core 서버로 RoundCompleteEvent 발행 완료.");
    }

    /**
     * 2. 라운드 종료 메시지 전송
     * 앱이 '/app/round/end'로 보낸 메시지를 처리
     * 라우팅 키: topic.round.end
     */
    @MessageMapping("/round/end")
    public void handleRoundEnd(@Payload RoundEndEvent request) {

        log.info("[WS] 라운드 종료 결과 수신.");

//        RoundEndEvent event = toRoundEndEvent(request);

        rabbitMqBroker.publish(request);

        log.info("Core 서버로 RoundEndEvent 발행 완료. 다음 라운드 준비 요청.");
    }

    /**
     * 3. 프로토콜 최종 완료 메시지 전송
     * 앱이 '/app/protocol/complete'로 보낸 메시지를 처리
     * 라우팅 키: topic.complete.{memberId}
     */
    @MessageMapping("/protocol/complete")
    public void handleProtocolComplete(@Payload ProtocolCompleteEvent request) {

        log.info("[WS] 프로토콜 최종 완료 결과 수신. Client ID: {}", request.memberId());

//        ProtocolCompleteEvent event = toProtocolCompleteEvent(request);

        rabbitMqBroker.publish(request);

        log.info("Core 서버로 ProtocolCompleteEvent 발행 완료. 최종 처리 요청.");
    }
}