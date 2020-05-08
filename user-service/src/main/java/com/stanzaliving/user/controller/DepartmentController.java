package com.stanzaliving.user.controller;


import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.user.enums.EnumListing;
import com.stanzaliving.user.service.DepartmentService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *
 * @author piyush srivastava "piyush.srivastava@stanzaliving.com"
 *
 * @date 15-Apr-2020
 *
 */

@Log4j2
@RestController
@RequestMapping("department")
public class DepartmentController {

	@Autowired
	private DepartmentService departmentService;

	@GetMapping("list")
	public ResponseDto<List<EnumListing>> getUserDepartment() {

		log.info("Received Department listing request.");
		List<EnumListing> departmentList = departmentService.getAll();
		return ResponseDto.success("Found " + departmentList.size() + " Department", departmentList);
	}
}
