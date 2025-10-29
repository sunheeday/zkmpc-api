package com.zkrypto.zkmpc_api.domain.member.repository;

import com.zkrypto.zkmpc_api.domain.member.entity.Member;

import java.util.Optional;

public interface MemberRepository {
    Member save(Member member);
    Optional<Member> findByMemberId(String memberId);
    Optional<Member> findByAddress(String address);
}
