/**
 * 
 */
package com.stanzaliving.user.acl.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.user.acl.request.dto.UserAccessDto;
import com.stanzaliving.user.acl.service.AclService;

import lombok.extern.log4j.Log4j;

/**
 * @author naveen.kumar
 *
 * @date 23-Oct-2019
 *
 **/
@Log4j
@RestController
@RequestMapping("acl")
public class AclController {

	@Autowired
	private AclService aclService;

	@PostMapping("check")
	public ResponseDto<Boolean> isAccessible(@RequestBody @Valid UserAccessDto userAccessDto) {

		log.info("Checking User: " + userAccessDto.getUserId() + " access for url: " + userAccessDto.getUrl());

		boolean accessible = aclService.isAccesible(userAccessDto.getUserId(), userAccessDto.getUrl());

		log.info("URL: " + userAccessDto.getUrl() + " accessible by user: " + userAccessDto.getUserId() + " Status: " + accessible);

		return ResponseDto.success("URL Access Status: " + accessible, accessible);
	}
}