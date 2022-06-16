package com.stanzaliving.user.service;

public interface PauseOtpService {

	boolean checkIfNeedToStop(String mobile);
	
	boolean pauseOtp(String mobile);
	boolean pauseOtpV2(String mobile,String token);
	
	boolean resumeOtp(String mobile);
}
