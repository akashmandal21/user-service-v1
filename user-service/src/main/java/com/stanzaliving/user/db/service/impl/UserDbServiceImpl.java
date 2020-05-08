/**
 * 
 */
package com.stanzaliving.user.db.service.impl;

import com.stanzaliving.core.base.common.dto.PageResponse;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.dto.PageAndSortDto;
import com.stanzaliving.core.sqljpa.specification.utils.CriteriaOperation;
import com.stanzaliving.core.sqljpa.specification.utils.StanzaSpecificationBuilder;
import com.stanzaliving.core.user.dto.UserFilterDto;
import com.stanzaliving.core.user.enums.UserType;
import com.stanzaliving.user.constants.UserQueryConstants;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.stanzaliving.core.sqljpa.service.impl.AbstractJpaServiceImpl;
import com.stanzaliving.user.db.service.UserDbService;
import com.stanzaliving.user.entity.UserEntity;
import com.stanzaliving.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
@Service
public class UserDbServiceImpl extends AbstractJpaServiceImpl<UserEntity, Long, UserRepository> implements UserDbService {

	@Autowired
	private UserRepository userRepository;

	@Override
	protected UserRepository getJpaRepository() {
		return userRepository;
	}

	@Override
	public UserEntity getUserForMobile(String mobile, String isoCode) {
		return getJpaRepository().findByMobileAndIsoCode(mobile, isoCode);
	}



	private Pageable getPageable(PageAndSortDto pageAndSortDto) {
		Integer pageNo = Math.max(0, pageAndSortDto.getPageNo() - 1);
		Integer limit = Math.max(1, pageAndSortDto.getLimit());
		limit = Math.min(limit, 1000);

		Sort.Direction direction = StringUtils.isEmpty(pageAndSortDto.getSortOrder()) ? Sort.Direction.DESC : Sort.Direction.valueOf(pageAndSortDto.getSortOrder());
		String sortBy = StringUtils.isEmpty(pageAndSortDto.getSortBy()) ? "createdAt" : pageAndSortDto.getSortBy();
		return PageRequest.of(pageNo, limit, direction, sortBy);
	}


	@Override
	public Specification<UserEntity> getSearchQuery(UserFilterDto userFilterDto) {

		StanzaSpecificationBuilder<UserEntity> specificationBuilder = new StanzaSpecificationBuilder<>();

		if (CollectionUtils.isNotEmpty(userFilterDto.getUserIds())) {

			specificationBuilder.with(UserQueryConstants.UUID, CriteriaOperation.IN, userFilterDto.getUserIds());

		} else {

			if (StringUtils.isNotBlank(userFilterDto.getMobile())) {
				specificationBuilder.with(UserQueryConstants.MOBILE, CriteriaOperation.EQ, userFilterDto.getMobile());

				if (StringUtils.isNotBlank(userFilterDto.getIsoCode())) {
					specificationBuilder.with(UserQueryConstants.ISO_CODE, CriteriaOperation.EQ, userFilterDto.getIsoCode());
				}
			}

			if (StringUtils.isNotBlank(userFilterDto.getEmail())) {
				specificationBuilder.with(UserQueryConstants.EMAIL, CriteriaOperation.EQ, userFilterDto.getEmail());
			}

			if (Objects.nonNull(userFilterDto.getUserType())) {
				specificationBuilder.with(UserQueryConstants.USER_TYPE, CriteriaOperation.ENUM_EQ, userFilterDto.getUserType());
			}

			if (userFilterDto.getStatus() != null) {

				if (userFilterDto.getStatus()) {
					specificationBuilder.with(UserQueryConstants.STATUS, CriteriaOperation.TRUE, true);
				} else {
					specificationBuilder.with(UserQueryConstants.STATUS, CriteriaOperation.FALSE, false);
				}
			}

			if(Objects.nonNull(userFilterDto.getDepartment())){
				specificationBuilder.with(UserQueryConstants.DEPARTMENT, CriteriaOperation.ENUM_EQ, userFilterDto.getDepartment());
			}

			if(StringUtils.isNotBlank(userFilterDto.getName())){
				List<UserEntity> userEntities = userRepository.searchByName(userFilterDto.getName());
				if(CollectionUtils.isNotEmpty(userEntities)){
					List<String> userIdList = new ArrayList<>();
					userEntities.forEach(userEntity -> {userIdList.add(userEntity.getUuid());});
					specificationBuilder.with(UserQueryConstants.UUID, CriteriaOperation.IN, userIdList);
				} else{
					specificationBuilder.with(UserQueryConstants.UUID, CriteriaOperation.EQ, -1);
				}
			}
		}

		return specificationBuilder.build();
	}


	@Override
	public Page<UserEntity> findByUuids(List<String> userUuids, int pageNo, int limit) {
		PageAndSortDto pageAndSortDto = PageAndSortDto.builder()
				.limit(limit)
				.pageNo(pageNo)
				.build();

		UserFilterDto filterDto = UserFilterDto.builder()
				.userIds(userUuids).build();

		Specification<UserEntity> specification = getSearchQuery(filterDto);

		if (CollectionUtils.isEmpty(filterDto.getUserIds())) {
			return null;
		}

		return findAll(specification, getPageable(pageAndSortDto));
	}
}