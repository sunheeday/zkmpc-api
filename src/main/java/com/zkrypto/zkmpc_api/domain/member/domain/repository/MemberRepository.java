package com.zkrypto.zkmpc_api.domain.member.domain.repository;

import com.zkrypto.zkmpc_api.domain.member.domain.entity.Member;

import java.util.Optional;

public interface MemberRepository {
    Member save(Member member);
    Optional<Member> findByMemberId(String memberId);
    Optional<Member> findByAddress(String address);
    Optional<Member> findByEmail(String email);
    Optional<Member> findByGroup_GroupId(String groupId);

}
