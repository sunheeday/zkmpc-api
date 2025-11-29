package com.zkrypto.zkmpc_api.infrastructure;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate; // 외부 통신을 위해 RestTemplate 사용 가정
import java.util.List;
import java.util.Map;

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

        for (String id : memberIds) {
            log.info("member id : " + id);
        }

        log.info("process : {}", process);
        log.info("sid : {}", sid);
        log.info("memberIds : {}", memberIds);
        log.info("threshold : {}", threshold);
        log.info("messageBytes : {}", messageBytes);

        Map<String, Object> requestBody = Map.of(
                "process", process,
                "sid", sid,
                "memberIds", memberIds,
                "threshold", threshold,
                "messageBytes", messageBytes
        );

        try {
            restTemplate.postForEntity(url, requestBody, Object.class);
            System.out.println(">>> [zkMPC INFRA] " + process + " 프로토콜 시작 요청 성공: " + url);
        } catch (Exception e) {
            throw new RuntimeException(process + " 프로토콜 시작에 실패했습니다. URI: " + url, e);
        }
    }
}