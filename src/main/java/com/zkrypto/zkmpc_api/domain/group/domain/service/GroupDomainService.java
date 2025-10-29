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
        if (!List.of("KEY_GENERATION", "SIGNING", "REFRESH").contains(process)) {
            throw new IllegalArgumentException("유효하지 않은 zkMPC 프로세스 타입입니다.");
        }
        try {
            zkMpcClient.requestStart(process, sid, memberIds, threshold, messageBytes);
        } catch (Exception e) {
            throw new RuntimeException("zkMPC 서버와 통신 중 오류 발생", e);
        }
    }
}
