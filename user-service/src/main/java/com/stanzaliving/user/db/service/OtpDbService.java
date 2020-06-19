/**
 * 
 */
package com.stanzaliving.user.db.service;

import com.stanzaliving.core.sqljpa.service.AbstractJpaService;
import com.stanzaliving.core.user.enums.OtpType;
import com.stanzaliving.core.user.enums.UserType;
import com.stanzaliving.user.entity.OtpEntity;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
public interface OtpDbService extends AbstractJpaService<OtpEntity, Long> {

	OtpEntity getOtpForMobile(String mobile, OtpType otpType, String isoCode);

	OtpEntity getUserOtpByUserId(String userId, OtpType otpType);

	OtpEntity getActiveOtpForMobile(String mobile, OtpType otpType, UserType userType, String isoCode);

}