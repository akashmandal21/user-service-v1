package com.stanzaliving.user.acl.repository;

import com.stanzaliving.core.base.enums.AccessModule;
import com.stanzaliving.core.sqljpa.repository.AbstractJpaRepository;
import com.stanzaliving.user.acl.entity.RoleAccessModuleMappingEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleAccessModuleRepository extends AbstractJpaRepository<RoleAccessModuleMappingEntity, Long> {

    @Query(value = "SELECT DISTINCT r.accessModule " +
        "FROM com.stanzaliving.user.acl.entity.RoleAccessModuleMappingEntity r " +
        "WHERE r.roleUuid IN :roleUuids " +
        "AND r.status = :status")
    List<AccessModule> findAccessModuleByRoleUuidInAndStatus(List<String> roleUuids, boolean status);

    @Query(value = "SELECT r.roleUuid " +
        "FROM com.stanzaliving.user.acl.entity.RoleAccessModuleMappingEntity r " +
        "WHERE r.accessModule IN :accessModules")
    List<String> findRoleUuidByAccessModuleIn(List<AccessModule> accessModules);

    RoleAccessModuleMappingEntity findByRoleUuid(String roleUuid);
}
