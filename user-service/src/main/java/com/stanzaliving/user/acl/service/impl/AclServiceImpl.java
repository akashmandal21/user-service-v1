/**
 * 
 */
package com.stanzaliving.user.acl.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stanzaliving.user.acl.db.service.RoleDbService;
import com.stanzaliving.user.acl.service.AclService;

import lombok.extern.log4j.Log4j2;

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