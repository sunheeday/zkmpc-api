package com.zkrypto.zkmpc_api.domain.mpc.application.websocket.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.zkrypto.zkmpc_api.common.serializer.DelegateOutputDeserializer;
import com.zkrypto.zkmpc_api.domain.mpc.application.websocket.constant.DelegateOutputStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@AllArgsConstructor
@ToString
@JsonDeserialize(using = DelegateOutputDeserializer.class)
public class DelegateOutput {
    private DelegateOutputStatus delegateOutputStatus;
    private List<ContinueMessage> continueMessages;
    private Object doneMessage;
}
