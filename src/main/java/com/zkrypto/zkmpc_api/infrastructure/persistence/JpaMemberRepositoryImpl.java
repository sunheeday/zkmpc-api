package com.zkrypto.zkmpc_api.infrastructure.persistence;

import com.zkrypto.zkmpc_api.domain.member.entity.Member;
import com.zkrypto.zkmpc_api.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaMemberRepositoryImpl implements MemberRepository {
    private final JpaMemberRepository jpaRepository;

    public JpaMemberRepositoryImpl(JpaMemberRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Member save(Member member) {
        return jpaRepository.save(member);
    }

    @Override
    public Optional<Member> findByMemberId(String memberId) {
        return jpaRepository.findByMemberId(memberId);
    }

    @Override
    public Optional<Member> findByAddress(String address) {
        return jpaRepository.findByAddress(address);
    }
}
