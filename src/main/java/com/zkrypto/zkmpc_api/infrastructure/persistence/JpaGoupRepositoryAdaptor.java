package com.zkrypto.zkmpc_api.infrastructure.persistence;

import com.zkrypto.zkmpc_api.domain.group.domain.entity.Group;
import com.zkrypto.zkmpc_api.domain.group.domain.repository.GroupRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public class JpaGoupRepositoryAdaptor implements GroupRepository {
    private final JpaGroupRepository jpaGroupRepository;

    public JpaGoupRepositoryAdaptor(JpaGroupRepository jpaGroupRepository) {
        this.jpaGroupRepository = jpaGroupRepository;
    }

    @Override
    public Group save(Group group) {
        return jpaGroupRepository.save(group);
    }

    @Override
    public Optional<Group> findByGroupId(String groupId) {
        return jpaGroupRepository.findByGroupId(groupId);
    }
}