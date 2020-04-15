package com.stanzaliving.user.service.impl;

import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.user.enums.EnumListing;
import com.stanzaliving.user.service.DepartmentService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
public class DepartmentServiceImpl implements DepartmentService {

	@Override
	public List<EnumListing> getAll() {
		List<EnumListing> data = new ArrayList<>();
		for (Department department: Department.values()) {
			data.add(
					EnumListing.builder()
							.key(department.name())
							.value(department.departmentName)
							.build()
			);
		}
		return data;
	}
}
