/**
 * 
 */
package com.stanzaliving.user.controller.internal;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.user.dto.UserPropertyAndProfileMappingDto;
import com.stanzaliving.user.service.UserPropertyMappingService;

import lombok.extern.log4j.Log4j2;

/**
 * @author manish.pareek
 *
 * @date 01-OCT-2021
 */
@Log4j2
@RestController
@RequestMapping("internal/mapping")
public class InternalUserPropertyMappingController {

	@Autowired
	private UserPropertyMappingService userPropertyMappingService;

	@GetMapping("property/users")
	public ResponseDto<List<UserPropertyAndProfileMappingDto>> getUsersMappedToProperty(@RequestParam(name = "propertyId") String propertyId) {
		
		log.info("Fetching Mapped Properties with UserId: " + propertyId);

		List<UserPropertyAndProfileMappingDto> userPropertyMappings = userPropertyMappingService.getUsersMappedToProperty(propertyId);

		return ResponseDto.success("Found " + userPropertyMappings.size() + " Users mapped to Property", userPropertyMappings);
	}

}