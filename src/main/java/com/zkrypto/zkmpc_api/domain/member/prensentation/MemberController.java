package com.zkrypto.zkmpc_api.domain.member.prensentation;

import com.zkrypto.zkmpc_api.common.response.ApiResponse;
import com.zkrypto.zkmpc_api.domain.member.application.dto.*;
import com.zkrypto.zkmpc_api.domain.member.application.service.MemberService;
import com.zkrypto.zkmpc_api.domain.transaction.application.dto.TransactionResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService){
        this.memberService = memberService;
    }

    @PostMapping("/member")
    public ResponseEntity<ApiResponse<MemberIdResponse>> registerMember(@Valid @RequestBody VerifyEmailCode request) {
        MemberIdResponse response = memberService.verifyEmailCodeAndRegisterMember(request);

        return new ResponseEntity<>(ApiResponse.success(response), HttpStatus.OK);
    }

    @PostMapping("/member/email")
    public ResponseEntity<ApiResponse<Void>> requestEmail(@Valid @RequestBody EmailRequest request) {
        memberService.requestEmailVerificationCode(request.getEmail());
        return new ResponseEntity<>(ApiResponse.success(null), HttpStatus.OK);
    }

    @PostMapping("/member/address")
    public ResponseEntity<ApiResponse<Void>> registerAddress(@Valid @RequestBody AddressRegisterRequest request) {
        memberService.registerAddress(request);
        return new ResponseEntity<>(ApiResponse.success(null), HttpStatus.OK);
    }


    @PostMapping("/member/verify")
    public ResponseEntity<ApiResponse<MemberIdResponse>> verifyMember(@Valid @RequestBody VerifyEmailCode request) {
        MemberIdResponse response = memberService.verifyMember(request);
        return new ResponseEntity<>(ApiResponse.success(response), HttpStatus.OK);
    }

    @PostMapping("/member/recover")
    public ResponseEntity<ApiResponse<Void>> recoverKey(@Valid @RequestBody KeyRecoverRequest request) {
        memberService.recoverKey(request);
        return new ResponseEntity<>(ApiResponse.success(null), HttpStatus.OK);
    }
}

