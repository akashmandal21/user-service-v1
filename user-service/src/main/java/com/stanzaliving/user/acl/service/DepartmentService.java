/**
 * 
 */
package com.stanzaliving.user.acl.service;

import java.util.List;

import com.stanzaliving.core.user.acl.dto.DepartmentDto;

/**
 * @author naveen.kumar
 *
 * @date 13-Nov-2019
 *
 **/
public interface DepartmentService {

	DepartmentDto createDepartment(String departmentName);

	DepartmentDto getDepartmentById(String departmentId);

	DepartmentDto updateDepartmentStatus(String departmentId, boolean status);

	List<DepartmentDto> getAllDepartments();

}