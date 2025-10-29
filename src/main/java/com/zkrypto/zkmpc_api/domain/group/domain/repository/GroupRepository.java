package com.zkrypto.zkmpc_api.domain.group.domain.repository;

import com.zkrypto.zkmpc_api.domain.group.domain.entity.Group;

import java.util.Optional;

public interface GroupRepository {
    Group save(Group group);
    Optional<Group> findByGroupId(String groupId);
}
