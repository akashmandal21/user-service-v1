package com.stanzaliving.user.acl.db.service.impl;

import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.sqljpa.service.impl.AbstractJpaServiceImpl;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelEntity;
import com.stanzaliving.user.acl.repository.UserDepartmentLevelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDepartmentLevelDbServiceImpl extends AbstractJpaServiceImpl<UserDepartmentLevelEntity, Long, UserDepartmentLevelRepository> {

    @Autowired
    UserDepartmentLevelRepository userDepartmentLevelRepository;

    @Override
    protected UserDepartmentLevelRepository getJpaRepository() {
        return userDepartmentLevelRepository;
    }

    public UserDepartmentLevelEntity findByUserUuidAndDepartmentAndStatus(String userUuid, Department department, boolean status) {
        return userDepartmentLevelRepository.findByUserUuidAndDepartmentAndStatus(userUuid, department, status);
    }

    public UserDepartmentLevelEntity findByUserUuidAndDepartment(String userUuid, Department department) {
        return userDepartmentLevelRepository.findByUserUuidAndDepartment(userUuid, department);
    }

    public List<UserDepartmentLevelEntity> findByUserUuidAndStatus(String userUuid, boolean status) {
        return userDepartmentLevelRepository.findByUserUuidAndStatus(userUuid, status);
    }
}
