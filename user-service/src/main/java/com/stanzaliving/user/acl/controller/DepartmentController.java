/**
 * 
 */
package com.stanzaliving.user.acl.controller;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.user.acl.dto.DepartmentDto;
import com.stanzaliving.user.acl.service.DepartmentService;

import lombok.extern.log4j.Log4j2;

/**
 * @author naveen.kumar
 *
 * @date 14-Nov-2019
 *
 **/
@Log4j2
@RestController
@RequestMapping("department")
public class DepartmentController {

	@Autowired
	private DepartmentService departmentService;

	@PostMapping("create")
	public ResponseDto<DepartmentDto> createDepartment(@RequestParam(name = "departmentName") String departmentName) {

		log.info("Received request to add new department: " + departmentName);

		return ResponseDto.success("Added department: " + departmentName, departmentService.createDepartment(departmentName));
	}

	@PutMapping("update/status/{departmentId}")
	public ResponseDto<DepartmentDto> updateDepartmentStatus(
			@PathVariable(name = "departmentId") String departmentId,
			@RequestParam(name = "status") @NotNull(message = "Status is mandatory to update") Boolean status) {

		log.info("Received request to update status to " + status + " for department: " + departmentId);

		return ResponseDto.success("Update department: " + departmentId + " status", departmentService.updateDepartmentStatus(departmentId, status));
	}

	@GetMapping("{departmentId}")
	public ResponseDto<DepartmentDto> getDepartmentById(
			@PathVariable(name = "departmentId") String departmentId) {

		log.info("Received request to get department: " + departmentId);

		return ResponseDto.success("Found department for Id: " + departmentId, departmentService.getDepartmentById(departmentId));
	}

	@GetMapping("all")
	public ResponseDto<List<DepartmentDto>> getAllDepartments() {

		log.info("Received request to get all departments");

		List<DepartmentDto> departmentDtos = departmentService.getAllDepartments();

		return ResponseDto.success("Found " + departmentDtos.size() + " Departments", departmentDtos);
	}

}