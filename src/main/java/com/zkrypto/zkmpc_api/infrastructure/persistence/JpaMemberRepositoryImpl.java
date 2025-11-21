package com.zkrypto.zkmpc_api.infrastructure.persistence;

import com.zkrypto.zkmpc_api.domain.member.domain.entity.Member;
import com.zkrypto.zkmpc_api.domain.member.domain.repository.MemberRepository;
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
    public Optional<Member> findByEmail(String email){ return jpaRepository.findByEmail(email);}

    @Override
    public Optional<Member> findByAddress(String address) {
        return jpaRepository.findByAddress(address);
    }

    @Override
    public Optional<Member> findByGroup_GroupId(String groupId) {
        return jpaRepository.findByGroup_GroupId(groupId);
    }
}