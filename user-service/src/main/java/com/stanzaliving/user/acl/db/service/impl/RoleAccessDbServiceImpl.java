package com.stanzaliving.user.acl.db.service.impl;

import com.stanzaliving.core.sqljpa.service.impl.AbstractJpaServiceImpl;
import com.stanzaliving.core.user.acl.enums.RoleAccessType;
import com.stanzaliving.user.acl.entity.RoleAccessEntity;
import com.stanzaliving.user.acl.repository.RoleAccessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleAccessDbServiceImpl extends AbstractJpaServiceImpl<RoleAccessEntity, Long, RoleAccessRepository>  {

    @Autowired
    RoleAccessRepository roleAccessRepository;

    @Override
    protected RoleAccessRepository getJpaRepository() {
        return roleAccessRepository;
    }


    public RoleAccessEntity findByRoleUuidAndAccessUuidAndRoleAccessType(String roleUuid, String accessUuid, RoleAccessType roleAccessType) {
        return getJpaRepository().findByRoleUuidAndAccessUuidAndRoleAccessType(roleUuid, accessUuid, roleAccessType);
    }
}
