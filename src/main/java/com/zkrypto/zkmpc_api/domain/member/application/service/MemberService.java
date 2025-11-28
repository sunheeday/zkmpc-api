package com.zkrypto.zkmpc_api.domain.member.application.service;

import com.zkrypto.zkmpc_api.common.utility.U64IdGenerator;
import com.zkrypto.zkmpc_api.domain.group.domain.entity.Group;
import com.zkrypto.zkmpc_api.domain.member.application.dto.MemberRegisterRequest;
import com.zkrypto.zkmpc_api.domain.member.application.dto.MemberRegisterResponse;
import com.zkrypto.zkmpc_api.domain.member.domain.entity.Member;
import com.zkrypto.zkmpc_api.domain.member.domain.repository.MemberRepository;
import com.zkrypto.zkmpc_api.domain.member.domain.service.AuthCodeManager;
import com.zkrypto.zkmpc_api.domain.member.domain.service.EmailSender;
import com.zkrypto.zkmpc_api.domain.transaction.application.dto.TransactionResponse;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final AuthCodeManager authCodeManager; //레디스 구현체
    private final EmailSender emailSender;

    public MemberService(
            MemberRepository memberRepository,
            AuthCodeManager authCodeManager,
            EmailSender emailSender) {
        this.memberRepository = memberRepository;
        this.emailSender = emailSender;
        this.authCodeManager = authCodeManager;
    }

    // 1. 이메일 코드 요청 (POST /v1/member/email)
    public void requestEmailVerificationCode(String email) {
        if (memberRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일 주소입니다.");
        }

        String authCode = authCodeManager.generateCode();
        authCodeManager.save(email, authCode);

//        emailSender.sendVerificationCode(email, authCode);
    }

    // 2. 이메일 코드 검증 및 멤버 등록 (POST /v1/member)
    @Transactional
    public MemberRegisterResponse verifyEmailCodeAndRegisterMember(MemberRegisterRequest registerRequest) {

//        if (!validateAuthCode(registerRequest.getEmail(), registerRequest.getAuthCode())) {
//            throw new IllegalArgumentException("인증 코드가 일치하지 않거나 만료되었습니다.");
//        }

        if(memberRepository.findByEmail(registerRequest.getEmail()).isPresent()){
            throw new IllegalArgumentException("이미 가입된 이메일 주소입니다.");
        }


        String newMemberId = "1";

        Member member = new Member(
                newMemberId,
                registerRequest.getEmail()
        );

        memberRepository.save(member);
        authCodeManager.remove(registerRequest.getEmail());

        return new MemberRegisterResponse(newMemberId);
    }

    @Transactional
    public void setGroup(String memberId, Group group) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 멤버 ID입니다: " + memberId));

        member.setGroup(group);
    }

    private boolean validateAuthCode(String email, String authCode) {
        Optional<String> storedCodeOpt = authCodeManager.get(email);
        return storedCodeOpt.isPresent() && storedCodeOpt.get().equals(authCode);
    }
}
