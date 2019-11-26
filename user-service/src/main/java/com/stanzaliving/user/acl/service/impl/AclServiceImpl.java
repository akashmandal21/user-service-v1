/**
 * 
 */
package com.stanzaliving.user.acl.service.impl;

import com.stanzaliving.user.acl.service.AclService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * @author naveen.kumar
 *
 * @date 23-Oct-2019
 *
 **/
@Log4j2
@Service
public class AclServiceImpl implements AclService {


	@Override
	public boolean isAccesible(String userId, String url) {

		return false;
	}

}