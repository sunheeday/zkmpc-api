package com.zkrypto.zkmpc_api.domain.group.domain.service;

import com.zkrypto.zkmpc_api.infrastructure.ZkMpcClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupDomainService {
    private final ZkMpcClient zkMpcClient;
    // 필요한 경우 GroupRepository, EnterpriseRepository 등 주입

    public GroupDomainService(ZkMpcClient zkMpcClient) {
        this.zkMpcClient = zkMpcClient;
    }

    /**
     * zkMPC 프로토콜 시작 (KEY_GENERATION, SIGNING, REFRESH)
     */
    public void startProtocol(String process, String sid, List<String> memberIds, Integer threshold, byte[] messageBytes) {

        // 1. 도메인 규칙 검증 (예: process 타입 유효성, sid의 존재 유무 등)
        if (!List.of("KEY_GENERATION", "SIGNING", "REFRESH").contains(process)) {
            throw new IllegalArgumentException("유효하지 않은 zkMPC 프로세스 타입입니다.");
        }

        // 2. 인프라 계층의 클라이언트 호출 (외부 통신)
        try {
            zkMpcClient.requestStart(process, sid, memberIds, threshold, messageBytes);
        } catch (Exception e) {
            // 프로토콜 시작 실패 시 도메인 이벤트 발행 또는 특정 예외 발생
            throw new RuntimeException("zkMPC 서버와 통신 중 오류 발생", e);
        }

        // 3. 필요한 경우 엔티티 상태 변경 또는 이벤트 발행
    }
}
