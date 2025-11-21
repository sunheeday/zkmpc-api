package com.zkrypto.zkmpc_api.infrastructure.persistence;

import com.zkrypto.zkmpc_api.domain.member.domain.entity.Member;
import com.zkrypto.zkmpc_api.domain.member.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaMemberRepositoryAdaptor implements MemberRepository {

    private final JpaMemberRepository jpaMemberRepository;

    public JpaMemberRepositoryAdaptor(JpaMemberRepository jpaMemberRepository) {
        this.jpaMemberRepository = jpaMemberRepository;
    }

    @Override
    public Member save(Member member) {
        return jpaMemberRepository.save(member);
    }

    @Override
    public Optional<Member> findByMemberId(String memberId) {
        return jpaMemberRepository.findByMemberId(memberId);
    }

    @Override
    public Optional<Member> findByEmail(String email){ return jpaMemberRepository.findByEmail(email);}

    @Override
    public Optional<Member> findByAddress(String address) {
        return jpaMemberRepository.findByAddress(address);
    }

    @Override
    public Optional<Member> findByGroup_GroupId(String groupId) {
        return jpaMemberRepository.findByGroup_GroupId(groupId);
    }
}