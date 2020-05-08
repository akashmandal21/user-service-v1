package com.stanzaliving.user.adapters;

import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.user.enums.EnumListing;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

/**
 * @author piyush srivastava "piyush@stanzaliving.com"
 *
 * @date 16-Apr-2020
 *
 */

@Log4j2
@UtilityClass
public class DepartmentAdapter {
	public List<EnumListing> getDepartmentEnumAsEnumListing(){
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
