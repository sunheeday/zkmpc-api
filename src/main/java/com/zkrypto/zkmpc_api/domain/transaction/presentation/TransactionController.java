package com.zkrypto.zkmpc_api.domain.transaction.presentation;

import com.zkrypto.zkmpc_api.common.response.ApiResponse;
import com.zkrypto.zkmpc_api.domain.transaction.application.dto.TransactionListResponse;
import com.zkrypto.zkmpc_api.domain.transaction.application.dto.TransactionRequest;
import com.zkrypto.zkmpc_api.domain.transaction.application.dto.TransactionResponse;
import com.zkrypto.zkmpc_api.domain.transaction.application.dto.TransactionStatusUpdateRequest;
import com.zkrypto.zkmpc_api.domain.transaction.application.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/transaction")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // 1. 거래 요청 (POST)
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> requestTransaction(@Valid @RequestBody TransactionRequest request) {
        // Application Service 호출
        TransactionResponse response = transactionService.requestTransaction(request);

        // API 명세의 응답 형식에 맞춤
        return new ResponseEntity<>(
                ApiResponse.success(Map.of("transactionId", "1")),
                HttpStatus.OK
        );
    }

    // 2. 전체 목록 조회 (GET)
    // URI: api-server-ip/api/v1/transaction (전체 목록)
    @GetMapping
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransaction() {
            List<TransactionResponse> response = transactionService.getAllTransactions(); //
            return new ResponseEntity<>(ApiResponse.success(response), HttpStatus.OK);
    }

    // 3. 거래 상태 변경 (PATCH)
    @PatchMapping
    public ResponseEntity<ApiResponse<Void>> updateTransactionStatus(@Valid @RequestBody TransactionStatusUpdateRequest request) {
        transactionService.updateTransactionStatus(request);
        return new ResponseEntity<>(ApiResponse.success(null), HttpStatus.OK);
    }
}