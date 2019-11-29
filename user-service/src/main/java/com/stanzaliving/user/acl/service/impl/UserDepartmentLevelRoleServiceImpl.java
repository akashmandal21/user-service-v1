package com.stanzaliving.user.acl.service.impl;

import com.stanzaliving.user.acl.entity.UserDepartmentLevelRoleEntity;
import com.stanzaliving.user.acl.repository.UserDepartmentLevelRoleRepository;
import com.stanzaliving.user.acl.service.UserDepartmentLevelRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDepartmentLevelRoleServiceImpl implements UserDepartmentLevelRoleService {

    @Autowired
    UserDepartmentLevelRoleRepository userDepartmentLevelRoleRepository;

    @Override
    public List<UserDepartmentLevelRoleEntity> saveAll(List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntityList) {
        return userDepartmentLevelRoleRepository.saveAll(userDepartmentLevelRoleEntityList);
    }
}
