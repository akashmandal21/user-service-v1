/**
 * @author nipunaggarwal
 *
 */
package com.stanzaliving.user.controller.internal;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.user.enums.EnumListing;
import com.stanzaliving.user.adapters.DepartmentAdapter;

import lombok.extern.log4j.Log4j2;

/**
 * @author nipunaggarwal
 *
 */
@Log4j2
@RestController
@RequestMapping("internal/department")
public class InternalDepartmentController {

	@GetMapping("list")
	public ResponseDto<List<EnumListing<Department>>> getUserDepartment() {

		log.info("Received Department listing request.");
		return ResponseDto.success("Found Department List", DepartmentAdapter.getDepartmentEnumAsEnumListingSorted());
	}

}
