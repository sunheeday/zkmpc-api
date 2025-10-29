package com.zkrypto.zkmpc_api.domain.enterprise.presentation;

import com.zkrypto.zkmpc_api.common.response.ApiResponse;
import com.zkrypto.zkmpc_api.domain.enterprise.application.dto.EnterpriseRegisterRequest;
import com.zkrypto.zkmpc_api.domain.enterprise.application.dto.EnterpriseResponse;
import com.zkrypto.zkmpc_api.domain.enterprise.application.service.EnterpriseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/enterprise")
public class EnterpriseController {
    private final EnterpriseService enterpriseService;

    public EnterpriseController(EnterpriseService enterpriseService) {
        this.enterpriseService = enterpriseService;
    }

    // 1. 엔터프라이즈 등록 (POST /api/v1/enterprise)
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> registerEnterprise(@Valid @RequestBody EnterpriseRegisterRequest request) {
        enterpriseService.registerEnterprise(request);

        // 성공 응답 명세: 200 OK, data: null
        return new ResponseEntity<>(ApiResponse.success(null), HttpStatus.OK);
    }

    // 2. 엔터프라이즈 조회 (GET /api/v1/enterprise)
    @GetMapping
    public ResponseEntity<ApiResponse<List<EnterpriseResponse>>> getEnterprises() {
        List<EnterpriseResponse> response = enterpriseService.getAllEnterprises();

        // 성공 응답 명세: 200 OK, data: List<EnterpriseResponse>
        return new ResponseEntity<>(ApiResponse.success(response), HttpStatus.OK);
    }
}
