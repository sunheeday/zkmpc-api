package com.zkrypto.zkmpc_api.infrastructure.amqp;

import com.rabbitmq.client.Channel;
import com.zkrypto.dto.InitProtocolMessage;
import com.zkrypto.dto.ProceedRoundMessage;
import com.zkrypto.dto.StartProtocolMessage;
import com.zkrypto.zkmpc_api.common.annotation.ManualAck;
import com.zkrypto.zkmpc_api.config.RabbitMqConfig;
import com.zkrypto.zkmpc_api.domain.enterprise.domain.repository.EnterpriseRepository;
import com.zkrypto.zkmpc_api.domain.mpc.application.websocket.WebSocketService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageConsumer {

    private final WebSocketService webSocketService;
    private final EnterpriseRepository enterpriseRepository;
    private List<String> excludedIdsList;

    @PostConstruct
    public void init() {
        this.excludedIdsList = enterpriseRepository.findAllIds();
    }

    /**
     * 핵심 로직: 메시지를 필터링하고 TssService로 전달하는 메서드
     * @param message 수신된 메시지 객체 (ProceedRoundMessage, InitProtocolMessage 등)
     * @param receivedRoutingKey 라우팅 키
     * @throws IOException basicAck/basicNack 호출 실패 시
     */
    private void handleMessageDelivery(Object message, String receivedRoutingKey) throws IOException {
        String[] parts = receivedRoutingKey.split("\\.");
        String clientIdFromKey = parts[parts.length - 1];

        boolean isExcludedByList = excludedIdsList.contains(clientIdFromKey);
        boolean isNotNumeric = !clientIdFromKey.matches("\\d+");

        try {
            if (isExcludedByList||isNotNumeric) {
                log.warn("[Filter] 제외 대상. Routing Key: {} 처리 없이 ACK.", receivedRoutingKey);
            } else {
                log.info("메시지 수신 및 앱 전달 시작. Type: {} Client ID: {}", message.getClass().getSimpleName(), clientIdFromKey);
                webSocketService.deliverMessageToApp(clientIdFromKey, message);
            }

        } catch (Exception e) {
            log.error("메시지 처리/전달 중 예외 발생. Routing Key: {}", receivedRoutingKey, e);
        } finally {

        }
    }


    // --- 1. 라운드 메시지 처리 리스너 ---
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "tss.message.handle.all" , durable = "true", exclusive = "false", autoDelete = "false",
            arguments = {
                    @Argument(name = "x-dead-letter-exchange", value = RabbitMqConfig.TSS_DLX_EXCHANGE),
                    @Argument(name = "x-dead-letter-routing-key", value = RabbitMqConfig.TSS_DLQ_ROUTING_KEY)
            }),
            exchange = @Exchange(value = RabbitMqConfig.TSS_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = RabbitMqConfig.TSS_ROUND_ROUTING_KEY_PREFIX + "." + "#"
    ))
    public void handleTssMessage(ProceedRoundMessage message
            , @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String receivedRoutingKey
    ) throws IOException {
        handleMessageDelivery(message, receivedRoutingKey);
    }


    // --- 2. 프로토콜 시작 메시지 처리 리스너 ---
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "tss.start.all", durable = "true", exclusive = "false", autoDelete = "false",
                    arguments = {
                            @Argument(name = "x-dead-letter-exchange", value = RabbitMqConfig.TSS_DLX_EXCHANGE),
                            @Argument(name = "x-dead-letter-routing-key", value = RabbitMqConfig.TSS_DLQ_ROUTING_KEY)
                    }),
            exchange = @Exchange(value = RabbitMqConfig.TSS_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = RabbitMqConfig.TSS_START_ROUTING_KEY_PREFIX + "." + "#"
    ))
    public void startTssProtocol(StartProtocolMessage message,
                                 @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String receivedRoutingKey
    ) throws IOException {
        handleMessageDelivery(message, receivedRoutingKey);
    }

    // --- 3. 프로토콜 초기화 메시지 처리 리스너 ---
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "tss.init.all", durable = "true", exclusive = "false", autoDelete = "false",
                    arguments = {
                            @Argument(name = "x-dead-letter-exchange", value = RabbitMqConfig.TSS_DLX_EXCHANGE),
                            @Argument(name = "x-dead-letter-routing-key", value = RabbitMqConfig.TSS_DLQ_ROUTING_KEY)
                    }),
            exchange = @Exchange(value = RabbitMqConfig.TSS_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = RabbitMqConfig.TSS_INIT_ROUTING_KEY_PREFIX + "." + "#"
    ))
    public void initTssProtocol(InitProtocolMessage message,
                                @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String receivedRoutingKey
    ) throws IOException {
        log.info("initTssProtocol ReceivedRoutingKey:{}",receivedRoutingKey);
        handleMessageDelivery(message, receivedRoutingKey);
    }
}