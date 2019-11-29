package com.stanzaliving.user.acl.db.service;

import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.sqljpa.service.AbstractJpaService;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelEntity;

import java.util.List;

public interface UserDepartmentLevelDbService extends AbstractJpaService<UserDepartmentLevelEntity, Long> {
    UserDepartmentLevelEntity findByUserUuidAndDepartmentAndStatus(String userUuid, Department department, boolean status);

    UserDepartmentLevelEntity findByUserUuidAndDepartment(String userUuid, Department department);

    List<UserDepartmentLevelEntity> findByUserUuidAndStatus(String userUuid, boolean status);
}
