/**
 * 
 */
package com.stanzaliving.user.acl.service.impl;

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.stanzaliving.core.base.common.dto.PageResponse;
import com.stanzaliving.core.base.exception.StanzaException;
import com.stanzaliving.core.sqljpa.specification.utils.CriteriaOperation;
import com.stanzaliving.core.sqljpa.specification.utils.StanzaSpecificationBuilder;
import com.stanzaliving.core.user.acl.dto.RoleDto;
import com.stanzaliving.core.user.acl.dto.RoleMetadataDto;
import com.stanzaliving.core.user.acl.request.dto.AddRoleRequestDto;
import com.stanzaliving.core.user.acl.request.dto.UpdateRoleRequestDto;
import com.stanzaliving.user.acl.adapters.RoleAdapter;
import com.stanzaliving.user.acl.constants.QueryConstants;
import com.stanzaliving.user.acl.db.service.ApiDbService;
import com.stanzaliving.user.acl.db.service.RoleDbService;
import com.stanzaliving.user.acl.entity.ApiEntity;
import com.stanzaliving.user.acl.entity.RoleEntity;
import com.stanzaliving.user.acl.service.RoleService;

import lombok.extern.log4j.Log4j2;

/**
 * @author naveen.kumar
 *
 * @date 22-Oct-2019
 *
 **/
@Log4j2
@Service
public class RoleServiceImpl implements RoleService {

	@Autowired
	private ApiDbService apiDbService;

	@Autowired
	private RoleDbService roleDbService;

	@Override
	public RoleDto addRole(AddRoleRequestDto addRoleRequestDto) {

		log.info("Checking if role already exists with name: " + addRoleRequestDto.getRoleName());

		if (roleDbService.isRoleExists(addRoleRequestDto.getRoleName())) {
			log.error("Role already exists with name: " + addRoleRequestDto.getRoleName());
			throw new StanzaException("Role Already exists with name: " + addRoleRequestDto.getRoleName());
		}

		log.info("Adding new role with name: " + addRoleRequestDto.getRoleName());

		List<ApiEntity> apiEntities = apiDbService.findByUuidIn(addRoleRequestDto.getApiIds());

		RoleEntity roleEntity =
				RoleEntity.builder()
						.roleName(addRoleRequestDto.getRoleName())
						.apiEntities(apiEntities)
						.build();

		roleEntity = roleDbService.save(roleEntity);

		log.info("Added New Role[Name: " + roleEntity.getRoleName() + ", UUID: " + roleEntity.getUuid() + "]");

		return RoleAdapter.getDto(roleEntity);
	}

	@Override
	public RoleDto updateRole(UpdateRoleRequestDto updateRoleRequestDto) {

		RoleEntity roleEntity = roleDbService.findByUuid(updateRoleRequestDto.getRoleId());

		if (Objects.isNull(roleEntity)) {
			throw new StanzaException("No Role found with id: " + updateRoleRequestDto.getRoleId() + " to update");
		}

		if (!roleEntity.getRoleName().equals(updateRoleRequestDto.getRoleName())
				&& roleDbService.isRoleExists(updateRoleRequestDto.getRoleName())) {

			log.error("Role already exists with name: " + updateRoleRequestDto.getRoleName());
			throw new StanzaException("Role Already exists with name: " + updateRoleRequestDto.getRoleName());
		}

		log.info("Updating Role: " + updateRoleRequestDto.getRoleId());

		List<ApiEntity> apiEntities = apiDbService.findByUuidIn(updateRoleRequestDto.getApiIds());

		roleEntity.setRoleName(updateRoleRequestDto.getRoleName());
		roleEntity.setApiEntities(apiEntities);

		roleEntity = roleDbService.update(roleEntity);

		log.info("Updated Role [Name: " + roleEntity.getRoleName() + ", UUID: " + roleEntity.getUuid() + "]");

		return RoleAdapter.getDto(roleEntity);
	}

	@Override
	public List<RoleMetadataDto> getAllRoleNames() {

		log.info("Fetching All Role Names");

		List<RoleEntity> roleEntities = roleDbService.findAll(Sort.by(Direction.ASC, QueryConstants.Role.ROLE_NAME));

		return RoleAdapter.getMetadataDto(roleEntities);

	}

	@Override
	public RoleDto getRoleById(String roleId) {

		RoleEntity roleEntity = roleDbService.findByUuid(roleId);

		if (Objects.isNull(roleEntity)) {
			throw new StanzaException("No Role found with id: " + roleId);
		}

		log.info("Found Role with Id: " + roleId);

		return RoleAdapter.getDto(roleEntity);
	}

	@Override
	public PageResponse<RoleDto> searchRole(String roleName, Boolean status, int pageNo, int limit) {

		Page<RoleEntity> rolePage = getRolePage(roleName, status, pageNo, limit);

		log.info("Found " + rolePage.getNumberOfElements() + " Role Records on Page: " + pageNo + " for Search Criteria");

		List<RoleDto> apiDtos = RoleAdapter.getDto(rolePage.getContent());

		return new PageResponse<>(pageNo, rolePage.getNumberOfElements(), rolePage.getTotalPages(), rolePage.getTotalElements(), apiDtos);

	}

	private Page<RoleEntity> getRolePage(String roleName, Boolean status, int pageNo, int limit) {

		Specification<RoleEntity> specification = getSearchQuery(roleName, status);

		Pageable pagination = getPaginationForSearchRequest(pageNo, limit);

		return roleDbService.findAll(specification, pagination);
	}

	private Specification<RoleEntity> getSearchQuery(String roleName, Boolean status) {

		StanzaSpecificationBuilder<RoleEntity> specificationBuilder = new StanzaSpecificationBuilder<>();

		if (StringUtils.isNotBlank(roleName)) {
			specificationBuilder.with(QueryConstants.Role.ROLE_NAME, CriteriaOperation.LIKE, roleName + "%");
		}

		if (status != null) {

			if (status) {
				specificationBuilder.with(QueryConstants.STATUS, CriteriaOperation.TRUE, true);
			} else {
				specificationBuilder.with(QueryConstants.STATUS, CriteriaOperation.FALSE, false);
			}
		}

		return specificationBuilder.build();
	}

	private Pageable getPaginationForSearchRequest(int pageNo, int limit) {

		Pageable pagination = PageRequest.of(0, 10, Direction.DESC, "createdAt");

		if (pageNo > 0 && limit > 0 && limit < 1000) {
			pagination = PageRequest.of(pageNo - 1, limit, Direction.DESC, "createdAt");
		}

		return pagination;
	}
}