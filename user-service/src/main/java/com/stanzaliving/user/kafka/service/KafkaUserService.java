/**
 * 
 */
package com.stanzaliving.user.kafka.service;

import com.stanzaliving.core.pojo.SmsDto;
import com.stanzaliving.user.entity.OtpEntity;

/**
 * @author naveen
 *
 * @date 12-Oct-2019
 */
public interface KafkaUserService {

	void sendOtpToKafka(OtpEntity otpEntity);

	void sendSmsToKafka(SmsDto smsDto);

}