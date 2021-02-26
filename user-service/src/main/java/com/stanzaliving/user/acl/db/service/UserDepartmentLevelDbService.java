package com.stanzaliving.user.acl.db.service;

import com.stanzaliving.core.base.enums.AccessLevel;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.sqljpa.service.AbstractJpaService;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelEntity;

import java.util.List;
import java.util.Set;

public interface UserDepartmentLevelDbService extends AbstractJpaService<UserDepartmentLevelEntity, Long> {
    List<UserDepartmentLevelEntity> findByUserUuidAndDepartmentAndStatus(String userUuid, Department department, boolean status);

    List<UserDepartmentLevelEntity> findByUserUuidAndDepartment(String userUuid, Department department);

    List<UserDepartmentLevelEntity> findByUserUuidAndStatus(String userUuid, boolean status);

    UserDepartmentLevelEntity findByUserUuidAndDepartmentAndAccessLevelAndStatus(String userUuid, Department department, AccessLevel accessLevel, boolean status);

    List<UserDepartmentLevelEntity> findByUserUuidAndDepartmentAndAccessLevel(String userUuid, Department department, AccessLevel accessLevel);

    List<UserDepartmentLevelEntity> findByUuidInAndAccessLevel(List<String> uuids, AccessLevel accessLevel);

	List<UserDepartmentLevelEntity> findByUserUuidAndUuidIn(String userUuid, Set<String> departmentUuids);
}
