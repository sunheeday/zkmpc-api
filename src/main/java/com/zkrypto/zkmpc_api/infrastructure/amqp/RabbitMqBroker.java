package com.zkrypto.zkmpc_api.infrastructure.amqp;

import com.zkrypto.dto.*;
import com.zkrypto.zkmpc_api.config.RabbitMqConfig;
import com.zkrypto.zkmpc_api.domain.mpc.application.message.MessageBroker;
import com.zkrypto.zkmpc_api.domain.mpc.application.message.dto.InitProtocolEndEvent;
import com.zkrypto.zkmpc_api.domain.mpc.application.message.dto.ProtocolCompleteEvent;
import com.zkrypto.zkmpc_api.domain.mpc.application.message.dto.RoundCompleteEvent;
import com.zkrypto.zkmpc_api.domain.mpc.application.message.dto.RoundEndEvent;
import com.zkrypto.zkmpc_api.infrastructure.amqp.mapper.MessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMqBroker implements MessageBroker {

    private final RabbitTemplate rabbitTemplate;

    //all round ends
    @Override
    public void publish(RoundEndEvent event) {
        String routingKey = RabbitMqConfig.TSS_ROUND_END_ROUTING_KEY_PREFIX; //topic.round.end
        ProceedRoundMessage message = MessageMapper.from(event);
        rabbitTemplate.convertAndSend(RabbitMqConfig.TSS_EXCHANGE, routingKey, message);
    }

    @Override
    public void publish(InitProtocolEndEvent event) {
        String routingKey = RabbitMqConfig.TSS_INIT_END_ROUTING_KEY_PREFIX; //topic.init.end
        InitProtocolEndMessage message = MessageMapper.from(event);
        rabbitTemplate.convertAndSend(RabbitMqConfig.TSS_EXCHANGE, routingKey, message);
    }

    @Override
    public void publish(ProtocolCompleteEvent event) {
        //topic.complete
        String routingKey = RabbitMqConfig.TSS_PROTOCOL_COMPLETE_KEY_PREFIX; //topic.complete
        ProtocolCompleteMessage message = MessageMapper.from(event);
        rabbitTemplate.convertAndSend(RabbitMqConfig.TSS_EXCHANGE, routingKey, message);
    }

    //round 종료 후 다음 round's round 준비 알림
    @Override
    public void publish(RoundCompleteEvent event) {
        String routingKey = RabbitMqConfig.TSS_ROUND_COMPLETE_KEY_PREFIX; //topic.round.complete
        RoundCompleteMessage message = MessageMapper.from(event);
        rabbitTemplate.convertAndSend(RabbitMqConfig.TSS_EXCHANGE, routingKey, message);
    }
}
