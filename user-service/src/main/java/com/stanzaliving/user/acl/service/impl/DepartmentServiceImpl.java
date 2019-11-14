/**
 * 
 */
package com.stanzaliving.user.acl.service.impl;

import java.util.List;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stanzaliving.core.base.exception.NoRecordException;
import com.stanzaliving.core.base.exception.StanzaException;
import com.stanzaliving.core.user.acl.dto.DepartmentDto;
import com.stanzaliving.user.acl.adapters.DepartmentAdapter;
import com.stanzaliving.user.acl.db.service.DepartmentDbService;
import com.stanzaliving.user.acl.entity.DepartmentEntity;
import com.stanzaliving.user.acl.service.DepartmentService;
import com.stanzaliving.user.exception.MappingNotFoundException;

import lombok.extern.log4j.Log4j2;

/**
 * @author naveen.kumar
 *
 * @date 13-Nov-2019
 *
 **/
@Log4j2
@Service
public class DepartmentServiceImpl implements DepartmentService {

	@Autowired
	private DepartmentDbService departmentDbService;

	@Override
	public DepartmentDto createDepartment(String departmentName) {

		DepartmentEntity departmentEntity = departmentDbService.getDepartmentByName(departmentName);

		if (Objects.nonNull(departmentEntity)) {
			throw new StanzaException("Department: " + departmentName + " already exists");
		}

		log.info("Adding new Department: " + departmentName);

		departmentEntity =
				DepartmentEntity.builder()
						.departmentName(departmentName)
						.build();

		departmentEntity = departmentDbService.save(departmentEntity);

		return DepartmentAdapter.getDto(departmentEntity);
	}

	@Override
	public DepartmentDto getDepartmentById(String departmentId) {

		DepartmentEntity departmentEntity = departmentDbService.findByUuid(departmentId);

		if (Objects.isNull(departmentEntity)) {
			throw new MappingNotFoundException("Department Not Found With Id: " + departmentId);
		}

		log.info("Found Department: " + departmentEntity);

		return DepartmentAdapter.getDto(departmentEntity);
	}

	@Override
	public DepartmentDto updateDepartmentStatus(String departmentId, boolean status) {

		DepartmentEntity departmentEntity = departmentDbService.findByUuid(departmentId);

		if (Objects.isNull(departmentEntity)) {
			throw new StanzaException("Department Not Found With Id: " + departmentId);
		}

		log.info("Updating Department: " + departmentId + " status to " + status);

		departmentEntity.setStatus(status);

		departmentEntity = departmentDbService.save(departmentEntity);

		return DepartmentAdapter.getDto(departmentEntity);
	}

	@Override
	public List<DepartmentDto> getAllDepartments() {

		List<DepartmentEntity> departmentEntities = departmentDbService.findAllByStatus(true);

		if (CollectionUtils.isEmpty(departmentEntities)) {
			throw new NoRecordException("No Departments Found");
		}

		log.info("Found " + departmentEntities.size() + " Departments");

		return DepartmentAdapter.getDto(departmentEntities);
	}

}