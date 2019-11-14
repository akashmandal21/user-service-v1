/**
 * 
 */
package com.stanzaliving.user.acl.db.service;

import com.stanzaliving.core.sqljpa.service.AbstractJpaService;
import com.stanzaliving.user.acl.entity.DepartmentEntity;

/**
 * @author naveen.kumar
 *
 * @date 13-Nov-2019
 *
 **/
public interface DepartmentDbService extends AbstractJpaService<DepartmentEntity, Long> {

	DepartmentEntity getDepartmentByName(String departmentName);
}