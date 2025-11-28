package com.zkrypto.zkmpc_api.infrastructure;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;
import java.util.HashMap; // 🌟 HashMap을 사용하기 위해 import 추가

@Slf4j
@Component
public class ZkMpcClient {

    private static final String START_PROTOCOL_URI = "/api/v1/tss/start";

    @Value("${spring.zkmpc.core-server-ip}")
    private String coreServerIp;

    /**
     * 외부 zkMPC 서버에 프로토콜 시작 요청을 보냅니다.
     */
    public void requestStartProtocol(String process, String sid, List<String> memberIds, Integer threshold, byte[] messageBytes) {

        RestTemplate restTemplate = new RestTemplate();

        String url = coreServerIp + START_PROTOCOL_URI;

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("process", process);
        requestBody.put("sid", sid);
        requestBody.put("memberIds", memberIds);
        requestBody.put("threshold", threshold);
        requestBody.put("messageBytes", messageBytes);

        log.debug(">>> [zkMPC INFRA] Request URL: {}", url);
        log.debug(">>> [zkMPC INFRA] Request Body: {}", requestBody);

        try {
            ResponseEntity<Object> responseEntity = restTemplate.postForEntity(url, requestBody, Object.class);

            int statusCode = responseEntity.getStatusCodeValue();
            Object responseBody = responseEntity.getBody();

            log.info("<<< [zkMPC INFRA] Response Status: {} for {} protocol.", statusCode, process);
            log.info(">>> [zkMPC INFRA] {} 프로토콜 시작 요청 성공: {}", process, url);

        } catch (Exception e) {
            log.error("⚠️ [zkMPC INFRA] {} 프로토콜 시작에 실패했습니다. URI: {}", process, url, e);
            throw new RuntimeException(process + " 프로토콜 시작에 실패했습니다. URI: " + url, e);
        }
    }
}