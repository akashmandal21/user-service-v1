package com.stanzaliving.user.service.impl;

import com.stanzaliving.core.user.enums.EnumListing;
import com.stanzaliving.user.adapters.DepartmentAdapter;
import com.stanzaliving.user.service.DepartmentService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@Service
public class DepartmentServiceImpl implements DepartmentService {

	@Override
	public List<EnumListing> getAll() {
		return DepartmentAdapter.getDepartmentEnumAsEnumListing();
	}
}
