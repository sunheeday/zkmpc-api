package com.zkrypto.zkmpc_api.domain.member.prensentation;

import com.zkrypto.zkmpc_api.common.response.ApiResponse;
import com.zkrypto.zkmpc_api.domain.member.application.dto.MemberRegisterRequest;
import com.zkrypto.zkmpc_api.domain.member.application.service.MemberService;
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
    public ResponseEntity<ApiResponse<Void>> registerMember(@Valid @RequestBody MemberRegisterRequest request) {
        memberService.verifyEmailCodeAndRegisterMember(request);
        return new ResponseEntity<>(ApiResponse.success(null), HttpStatus.OK);
    }

    @PostMapping("/member/email")
    public ResponseEntity<ApiResponse<Void>> requestEmail(@Valid @RequestBody String email) {
        memberService.requestEmailVerificationCode(email);
        return new ResponseEntity<>(ApiResponse.success(null), HttpStatus.OK);
    }
}
