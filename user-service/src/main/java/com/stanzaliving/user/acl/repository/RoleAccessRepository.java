package com.stanzaliving.user.acl.repository;

import com.stanzaliving.core.sqljpa.repository.AbstractJpaRepository;
import com.stanzaliving.core.user.acl.enums.RoleAccessType;
import com.stanzaliving.user.acl.entity.RoleAccessEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleAccessRepository extends AbstractJpaRepository<RoleAccessEntity, Long> {

    RoleAccessEntity findByRoleUuidAndAccessUuidAndRoleAccessType(String roleUuid, String accessUuid, RoleAccessType roleAccessType);

}
