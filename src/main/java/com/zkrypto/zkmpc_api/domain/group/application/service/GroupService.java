package com.zkrypto.zkmpc_api.domain.group.application.service;

import com.zkrypto.zkmpc_api.domain.enterprise.domain.entity.Enterprise;
import com.zkrypto.zkmpc_api.domain.enterprise.domain.repository.EnterpriseRepository;
import com.zkrypto.zkmpc_api.domain.group.application.dto.GroupRegisterRequest;
import com.zkrypto.zkmpc_api.domain.group.domain.entity.Group;
import com.zkrypto.zkmpc_api.domain.group.domain.repository.GroupRepository;
import com.zkrypto.zkmpc_api.domain.group.domain.service.GroupDomainService;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupDomainService groupDomainService;
    private final EnterpriseRepository enterpriseRepository;

    public GroupService(
            GroupRepository groupRepository,
            GroupDomainService groupDomainService,
            EnterpriseRepository enterpriseRepository
    ) {
        this.groupRepository = groupRepository;
        this.groupDomainService = groupDomainService;
        this.enterpriseRepository = enterpriseRepository;
    }

    // 1. 그룹 등록 (POST /v1/group)
    @Transactional
    public void registerGroup(GroupRegisterRequest request) {
        String newGroupId = "G-" + UUID.randomUUID().toString();

        String initialMemberId = request.getMemberId();


        Set<Enterprise> enterprises = request.getEnterprises().stream()
                .map(enterpriseId -> enterpriseRepository.findByEnterpriseId(enterpriseId)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Enterprise ID가 포함되어 있습니다: " + enterpriseId)))
                .collect(Collectors.toSet());

        if (enterprises.size() < 2) {
            throw new IllegalArgumentException("그룹 등록을 위해서는 최소 2개 이상의 유효한 엔터프라이즈 ID가 필요합니다.");
        }

        List<String> memberIds = enterprises.stream()
                .map(Enterprise::getEnterpriseId)
                .collect(Collectors.toCollection(ArrayList::new));
        memberIds.add(0, initialMemberId);

        //여기 그 고정된 threshold 값 계산하는 거 - ..!!
        // 1. 총 참가자 수 계산 (현재 멤버 1명 + 엔터프라이즈 N개)
        int totalParticipants = memberIds.size();

        // 2. 요구되는 Threshold 값 계산 (총 참가자 수 - 1)
        int requiredThreshold = totalParticipants - 1;

        // 3. 현재 요청된 threshold 값과 비교
        if (!request.getThreshold().equals(requiredThreshold)) {
            throw new IllegalArgumentException(
                    "Threshold (" + request.getThreshold() +
                            ") 값은 총 참가자 수 (" + totalParticipants + ") - 1 인 " +
                            requiredThreshold + "이어야 합니다."
            );
        }

        // 1.2. 그룹 엔티티 생성 및 저장 (조회된 Enterprise 엔티티 Set을 전달)
        Group group = new Group(
                newGroupId,
                enterprises,
                request.getThreshold()
        );
        groupRepository.save(group);

        // 2. KEY_GENERATION 프로토콜 시작
        final String PROCESS_KEY_GENERATION = "KEY_GENERATION";

        try {
            groupDomainService.startProtocol(
                    PROCESS_KEY_GENERATION,
                    newGroupId,
                    memberIds,
                    request.getThreshold(),
                    null
            );
        } catch (Exception e) {
            throw new RuntimeException("KEY_GENERATION 프로토콜 시작 실패. 그룹 등록 취소됨.", e);
        }
    }

    // 2. zkMPC 프로토콜 시작 유스케이스 (POST /v1/tss/start)
    @Transactional
    public void startZkMpcProtocol(String process, String sid, List<String> memberIds, Integer threshold, byte[] messageBytes) {
        groupDomainService.startProtocol(process, sid, memberIds, threshold, messageBytes);
    }

    public Group getGroupById(String groupId) {
        return groupRepository.findByGroupId(groupId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 그룹 ID입니다: " + groupId));
    }

}