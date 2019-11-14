/**
 * 
 */
package com.stanzaliving.user.acl.db.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stanzaliving.core.sqljpa.service.impl.AbstractJpaServiceImpl;
import com.stanzaliving.user.acl.db.service.DepartmentDbService;
import com.stanzaliving.user.acl.entity.DepartmentEntity;
import com.stanzaliving.user.acl.repository.DepartmentRepository;

/**
 * @author naveen.kumar
 *
 * @date 13-Nov-2019
 *
 **/
@Service
public class DepartmentDbServiceImpl extends AbstractJpaServiceImpl<DepartmentEntity, Long, DepartmentRepository> implements DepartmentDbService {

	@Autowired
	private DepartmentRepository departmentRepository;

	@Override
	protected DepartmentRepository getJpaRepository() {
		return departmentRepository;
	}

	@Override
	public DepartmentEntity getDepartmentByName(String departmentName) {
		return getJpaRepository().findByDepartmentName(departmentName);
	}

}