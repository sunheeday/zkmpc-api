package com.zkrypto.zkmpc_api.domain.mpc.application.websocket;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zkrypto.dto.InitProtocolMessage;
import com.zkrypto.dto.ProceedRoundMessage;
import com.zkrypto.dto.StartProtocolMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketService {
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final SimpMessagingTemplate messagingTemplate;

    private static final String FIXED_DESTINATION = "/queue/messages/";

    /**
     * RabbitMQ로부터 받은 모든 메시지(라운드, 초기화, 시작)를 WebSocket으로 앱 클라이언트에게 전달
     * @param clientId 메시지를 수신할 앱의 고유 ID
     * @param message 앱에게 전달할 메시지 DTO (ProceedRoundMessage, InitProtocolMessage, StartProtocolMessage 등)
     */
    public void deliverMessageToApp(String clientId, Object message) {

        final String destination = FIXED_DESTINATION + clientId;
        try {
            messagingTemplate.convertAndSend(destination, message);

            log.info("Client {}에게 {} 메시지 성공적으로 전달. Destination: {}",
                    clientId, message.getClass().getSimpleName(), destination);

        } catch (Exception e) {
            log.error("클라이언트 {}에게 WebSocket 전송 중 실패했습니다. Destination: {}", clientId, destination, e);
            throw new RuntimeException("WebSocket message delivery failed for " + clientId, e);
        }
    }
}

//    /**
//     * RabbitMQ로부터 받은 메시지를 WebSocket을 통해 앱 클라이언트에게 전달합니다.
//     * @param clientId 메시지를 수신할 앱의 고유 ID
//     * @param message 앱에게 전달할 라운드 메시지 DTO
//     */
//    public void deliverProceedRound(String clientId, ProceedRoundMessage message) {
//
//        //convertAndSendToUser사용해서 /user/{clientId}/que/tss...되는거임
//        String destination = "/queue/tss-round-message";
//
//        try {
//            messagingTemplate.convertAndSendToUser(clientId, destination, message);
//            log.info("Client {}에게 라운드 메시지 성공적으로 전달.", clientId);
//
//        } catch (Exception e) {
//            log.error("클라이언트 {}에게 WebSocket 전송 중 실패했습니다.", clientId, e);
//
//            throw new RuntimeException("WebSocket message delivery failed for " + clientId, e);
//        }
//    }
//    /**
//     * 초기화 메시지를 WebSocket을 통해 앱 클라이언트에게 전달합니다.
//     * @param clientId 메시지를 수신할 앱의 고유 ID
//     * @param message 앱에게 전달할 초기화 메시지 DTO
//     */
//    public void deliverInitProtocol(String clientId, InitProtocolMessage message) {
//        String destination = "/queue/init";
//
//        try {
//            messagingTemplate.convertAndSendToUser(clientId, destination, message);
//            log.info("Client {}에게 초기화 메시지 전달 성공.", clientId);
//        } catch (Exception e) {
//            log.error("Client {}에게 초기화 메시지 WebSocket 전송 실패.", clientId, e);
//            throw new RuntimeException("Init message delivery failed.", e);
//        }
//    }
//
//    /**
//     * 프로토콜 시작 메시지를 WebSocket을 통해 앱 클라이언트에게 전달합니다.
//     * @param clientId 메시지를 수신할 앱의 고유 ID
//     * @param message 앱에게 전달할 시작 메시지 DTO
//     */
//    public void deliverStartProtocol(String clientId, StartProtocolMessage message) {
//        String destination = "/queue/start";
//
//        try {
//            messagingTemplate.convertAndSendToUser(clientId, destination, message);
//            log.info("Client {}에게 시작 메시지 전달 성공.", clientId);
//        } catch (Exception e) {
//            log.error("Client {}에게 시작 메시지 WebSocket 전송 실패.", clientId, e);
//            throw new RuntimeException("Start message delivery failed.", e);
//        }
//    }
//
//    private void convertOutputToKey(DelegateOutput output, String type, String sid){
//        String outputJsonString;
//        try {
//            outputJsonString = objectMapper.writeValueAsString(output);
//            getMasterKey(outputJsonString);
//        } catch (IOException e) {
//            log.error("Failed to serialize DelegateOutput to JSON", e);
//            throw new TssException(ErrorCode.JSON_PARSE_ERROR);
//
//        } catch (Exception e) {
//            log.error("TssBridge (getMasterKey) execution failed", e);
//            throw new TssException(ErrorCode.TSS_BRIDGE_ERROR);
//        }
//    }

