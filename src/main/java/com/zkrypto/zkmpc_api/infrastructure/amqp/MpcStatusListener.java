//package com.zkrypto.zkmpc_api.infrastructure.amqp;
//
//import com.zkrypto.zkmpc_api.domain.mpc.application.dto.MpcStatusUpdate;
//import com.zkrypto.zkmpc_api.infrastructure.external.MpcStatusPublisher;
//import lombok.RequiredArgsConstructor;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//public class MpcStatusListener {
//
//    private final MpcStatusPublisher statusPublisher;
//
//    // Core 서버가 상태 업데이트 메시지를 보낼 큐를구독
//    @RabbitListener(queues = "api.mpc.status.queue")
//    public void handleMpcStatusUpdate(MpcStatusUpdate update) {
//        //수신 후 전달
//        statusPublisher.publishMpcStatus(update);
//    }
//}