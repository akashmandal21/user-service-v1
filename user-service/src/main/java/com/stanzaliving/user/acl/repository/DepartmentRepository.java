/**
 * 
 */
package com.stanzaliving.user.acl.repository;

import org.springframework.stereotype.Repository;

import com.stanzaliving.core.sqljpa.repository.AbstractJpaRepository;
import com.stanzaliving.user.acl.entity.DepartmentEntity;

/**
 * 
 * @author naveen.kumar
 *
 * @date 13-Nov-2019
 *
 */
@Repository
public interface DepartmentRepository extends AbstractJpaRepository<DepartmentEntity, Long> {

	DepartmentEntity findByDepartmentName(String departmentName);

}