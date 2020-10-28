package com.stanzaliving.user.service;

public interface BlacklistUserService {

	boolean checkIfUserIsBlacklisted(String mobile);
	
	boolean addToBlacklist(String mobile);
	
	boolean removeFromBlacklist(String mobile);
}
