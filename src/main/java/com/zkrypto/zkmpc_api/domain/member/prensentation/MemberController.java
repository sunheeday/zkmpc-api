package com.zkrypto.zkmpc_api.domain.member.prensentation;

import com.zkrypto.zkmpc_api.common.response.ApiResponse;
import com.zkrypto.zkmpc_api.domain.member.application.dto.MemberRegisterRequest;
import com.zkrypto.zkmpc_api.domain.member.application.dto.MemberRegisterResponse;
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
    public ResponseEntity<ApiResponse<MemberRegisterResponse>> registerMember(@Valid @RequestBody MemberRegisterRequest request) {
        MemberRegisterResponse response = memberService.verifyEmailCodeAndRegisterMember(request);

        return new ResponseEntity<>(ApiResponse.success(response), HttpStatus.OK);
    }

    @PostMapping("/member/email")
    public ResponseEntity<ApiResponse<Void>> requestEmail(@Valid @RequestBody String email) {
        memberService.requestEmailVerificationCode(email);
        return new ResponseEntity<>(ApiResponse.success(null), HttpStatus.OK);
    }
}
