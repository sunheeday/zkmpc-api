package com.zkrypto.zkmpc_api.infrastructure.persistence;

import com.zkrypto.zkmpc_api.domain.group.domain.entity.Group;
import com.zkrypto.zkmpc_api.domain.group.domain.repository.GroupRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaGroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByGroupId(String groupId);
}
