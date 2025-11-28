package com.zkrypto.zkmpc_api.domain.member.application.service;

import com.zkrypto.zkmpc_api.common.utility.U64IdGenerator;
import com.zkrypto.zkmpc_api.domain.enterprise.domain.entity.Enterprise;
import com.zkrypto.zkmpc_api.domain.enterprise.domain.repository.EnterpriseRepository;
import com.zkrypto.zkmpc_api.domain.group.domain.entity.Group;
import com.zkrypto.zkmpc_api.domain.group.domain.repository.GroupRepository;
import com.zkrypto.zkmpc_api.domain.member.application.dto.AddressRegisterRequest;
import com.zkrypto.zkmpc_api.domain.member.application.dto.KeyRecoverRequest;
import com.zkrypto.zkmpc_api.domain.member.application.dto.MemberIdResponse;
import com.zkrypto.zkmpc_api.domain.member.application.dto.VerifyEmailCode;
import com.zkrypto.zkmpc_api.domain.member.domain.entity.Member;
import com.zkrypto.zkmpc_api.domain.member.domain.repository.MemberRepository;
import com.zkrypto.zkmpc_api.domain.member.domain.service.AuthCodeManager;
import com.zkrypto.zkmpc_api.domain.member.domain.service.EmailSender;
import com.zkrypto.zkmpc_api.domain.transaction.application.dto.TransactionResponse;
import com.zkrypto.zkmpc_api.infrastructure.ZkMpcClient;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final GroupRepository groupRepository;
    private final AuthCodeManager authCodeManager;
    private final EmailSender emailSender;
    private final ZkMpcClient zkMpcClient;


    public MemberService(
            MemberRepository memberRepository,
            AuthCodeManager authCodeManager,
            EmailSender emailSender,
            EnterpriseRepository enterpriseRepository,
            GroupRepository groupRepository,
            ZkMpcClient zkMpcClient
            ) {
        this.memberRepository = memberRepository;
        this.emailSender = emailSender;
        this.authCodeManager = authCodeManager;
        this.enterpriseRepository = enterpriseRepository;
        this.groupRepository = groupRepository;
        this.zkMpcClient = zkMpcClient;

    }

    // 1. 이메일 코드 요청 (POST /v1/member/email)
    public void requestEmailVerificationCode(String email) {
        String authCode = authCodeManager.generateCode();
        authCodeManager.save(email, authCode);

        emailSender.sendVerificationCode(email, authCode);
    }

    // 2. 이메일 코드 검증 및 멤버 등록 (POST /v1/member)
    @Transactional
    public MemberIdResponse verifyEmailCodeAndRegisterMember(VerifyEmailCode registerRequest) {

        if (!validateAuthCode(registerRequest.getEmail(), registerRequest.getAuthCode())) {
            throw new IllegalArgumentException("인증 코드가 일치하지 않거나 만료되었습니다.");
        }

        if(memberRepository.findByEmail(registerRequest.getEmail()).isPresent()){
            throw new IllegalArgumentException("이미 가입된 이메일 주소입니다.");
        }


        String newMemberId = U64IdGenerator.generateU64Id();

        Member member = new Member(
                newMemberId,
                registerRequest.getEmail()
        );

        memberRepository.save(member);
        authCodeManager.remove(registerRequest.getEmail());

        return new MemberIdResponse(newMemberId);
    }

    //3. 주소 등록
    @Transactional
    public void registerAddress(AddressRegisterRequest registerRequest){
        Member member = memberRepository.findByMemberId(registerRequest.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 멤버 ID입니다"));

        member.setAddress(registerRequest.getAddress());
    }

    //4.이메일 사용자 인증
    @Transactional
    public MemberIdResponse verifyMember(VerifyEmailCode recoverRequest) {

        if (!validateAuthCode(recoverRequest.getEmail(), recoverRequest.getAuthCode())) {
            throw new IllegalArgumentException("인증 코드가 일치하지 않거나 만료되었습니다.");
        }

        Member member = memberRepository.findByEmail(recoverRequest.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("사용하신 주소는 등록되지 않은 멤버의 주소입니다."));

        authCodeManager.remove(recoverRequest.getEmail());

        return new MemberIdResponse(member.getMemberId());
    }


    //5.복구
    @Transactional
    public void recoverKey(KeyRecoverRequest recoverRequest){

        String memberId = recoverRequest.getMemberId();

        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 멤버 ID(" + memberId + ")에 연결된 그룹을 찾을 수 없습니다."));

        Group group = member.getGroup();
        String groupId = group.getGroupId();
        Integer threshold = group.getThreshold();

        List<String> enterpriseIds = new ArrayList<>(group.getEnterpriseIds());
        int limit = Math.min(enterpriseIds.size(), threshold-1);

        List<String> memberIds = new ArrayList<>(enterpriseIds.subList(0, limit));
        memberIds.add(memberId);

        try {
            zkMpcClient.requestStartProtocol(
                    "RECOVER",
                    groupId,
                    memberIds,
                    threshold,
                    null
            );
        } catch (Exception e) {
            throw new RuntimeException("RECOVER 프로토콜 시작 실패.", e);
        }
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
