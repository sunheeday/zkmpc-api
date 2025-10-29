package com.zkrypto.zkmpc_api.infrastructure;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate; // 외부 통신을 위해 RestTemplate 사용 가정
import java.util.List;
import java.util.Map;

@Component
public class ZkMpcClient {

    private static final String START_PROTOCOL_URI = "/api/v1/tss/start";

    // application.yml 등에 설정된 core-server-ip 값을 주입받음
    @Value("${zkmpc.core-server-ip}")
    private String coreServerIp;

    private final RestTemplate restTemplate;

    public ZkMpcClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 외부 zkMPC 서버에 프로토콜 시작 요청을 보냅니다.
     */
    public void requestStart(String process, String sid, List<String> memberIds, Integer threshold, byte[] messageBytes) {

        String url = coreServerIp + START_PROTOCOL_URI;

        Map<String, Object> requestBody = Map.of(
                "process", process,
                "sid", sid,
                "memberIds", memberIds,
                "threshold", threshold
                // SIGNING이 아닐 경우 messageBytes는 null이거나 생략될 수 있음
        );

        try {
            // POST 요청 실행. 응답은 200 OK Body: { "message": "Success", "data": null }
            restTemplate.postForEntity(url, requestBody, Object.class);
            System.out.println(">>> [zkMPC INFRA] " + process + " 프로토콜 시작 요청 성공: " + url);
        } catch (Exception e) {
            // 통신 실패나 4xx/5xx 응답 시 예외 처리
            throw new RuntimeException(process + " 프로토콜 시작에 실패했습니다. URI: " + url, e);
        }
    }
}