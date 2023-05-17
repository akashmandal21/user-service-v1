/**
 * 
 */
package com.stanzaliving.user.kafka.service;

import com.stanzaliving.core.enums.Source;
import com.stanzaliving.core.pojo.SmsDto;
import com.stanzaliving.core.user.acl.dto.RoleDto;
import com.stanzaliving.user.entity.OtpEntity;
import com.stanzaliving.user.entity.UserEntity;

/**
 * @author naveen
 *
 * @date 12-Oct-2019
 */
public interface KafkaUserService {

	void sendOtpToKafka(OtpEntity otpEntity, UserEntity userEntity);
	void sendOtpToKafkaV2(OtpEntity otpEntity, UserEntity userEntity, Source source);

	void sendSmsToKafka(SmsDto smsDto);

	void sendNewRoleToKafka(RoleDto roleDto);
}