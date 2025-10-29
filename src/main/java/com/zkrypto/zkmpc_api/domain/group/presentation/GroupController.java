package com.zkrypto.zkmpc_api.domain.group.presentation;

import com.zkrypto.zkmpc_api.common.response.ApiResponse;
import com.zkrypto.zkmpc_api.domain.group.application.dto.GroupRegisterRequest;
import com.zkrypto.zkmpc_api.domain.group.application.dto.ZkMpcStartRequest;
import com.zkrypto.zkmpc_api.domain.group.application.service.GroupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    // 1. 그룹 등록 (POST /api/v1/group)
    // 요청: GroupRegisterRequest (memberId, enterprises, threshold)
    @PostMapping("/group")
    public ResponseEntity<ApiResponse<Void>> registerGroup(@Valid @RequestBody GroupRegisterRequest request) {
        // Application Service 호출 (내부적으로 KEY_GENERATION 프로토콜 시작 포함)
        groupService.registerGroup(request);
        // API 명세에 따른 성공 응답 (data: null)
        return new ResponseEntity<>(ApiResponse.success(null), HttpStatus.OK);
    }

    // 2. zkMPC 프로토콜 시작 (POST /api/v1/tss/start)
    // 요청: ZkMpcStartRequest (process, sid, memberIds, threshold, messageBytes)
    @PostMapping("/tss/start")
    public ResponseEntity<ApiResponse<Void>> startProtocol(@Valid @RequestBody ZkMpcStartRequest request) {

        // Application Service 호출
        groupService.startZkMpcProtocol(
                request.getProcess(),
                request.getSid(),
                request.getMemberIds(),
                request.getThreshold(),
                request.getMessageBytes()
        );

        // API 명세에 따른 성공 응답 (data: null)
        return new ResponseEntity<>(ApiResponse.success(null), HttpStatus.OK);
    }
}