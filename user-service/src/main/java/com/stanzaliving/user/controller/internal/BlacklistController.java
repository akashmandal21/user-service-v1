package com.stanzaliving.user.controller.internal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.user.service.BlacklistUserService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequestMapping("/internal/blacklist")
public class BlacklistController {

	@Autowired
	private BlacklistUserService blacklistUserService;

	@GetMapping("add/{mobile}")
	public ResponseDto<Boolean> addUser(@PathVariable String mobile) {

		log.info("add User with mobile: {}", mobile);

		return ResponseDto.success("Added!!", blacklistUserService.addToBlacklist(mobile));
	}

	@GetMapping("delete/{mobile}")
	public ResponseDto<Boolean> deleteUser(@PathVariable String mobile) {

		log.info("add User with mobile: {}", mobile);

		return ResponseDto.success("Deleted", blacklistUserService.removeFromBlacklist(mobile));
	}

	@GetMapping("check/{mobile}")
	public ResponseDto<Boolean> checkUser(@PathVariable String mobile) {

		log.info("add User with mobile: {}", mobile);

		return ResponseDto.success("Chcked", blacklistUserService.checkIfUserIsBlacklisted(mobile));
	}

}