package com.zkrypto.zkmpc_api.infrastructure.persistence;

import com.zkrypto.zkmpc_api.domain.group.domain.entity.Group;
import com.zkrypto.zkmpc_api.domain.group.domain.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public class JpaGroupRepositoryImpl implements GroupRepository {

    @Autowired
    private JpaGroupRepository jpaRepository;

//    public JpaGroupRepositoryImpl(JpaGroupRepository jpaRepository) {
//        this.jpaRepository = jpaRepository;
//    }

    @Override
    public Group save(Group group) {
        return jpaRepository.save(group);
    }

    @Override
    public Optional<Group> findByGroupId(String groupId) {
        return jpaRepository.findByGroupId(groupId);
    }
}