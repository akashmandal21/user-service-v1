package com.stanzaliving.user.acl.repository;

import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.sqljpa.repository.AbstractJpaRepository;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDepartmentLevelRepository extends AbstractJpaRepository<UserDepartmentLevelEntity, Long> {

    UserDepartmentLevelEntity findByUserUuidAndDepartmentAndStatus(String userUuid, Department department, boolean status);

    UserDepartmentLevelEntity findByUserUuidAndDepartment(String userUuid, Department department);

    List<UserDepartmentLevelEntity> findByUserUuidAndStatus(String userUuid, boolean status);
}
