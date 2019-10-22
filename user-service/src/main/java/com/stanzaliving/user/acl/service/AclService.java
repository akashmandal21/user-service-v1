/**
 * 
 */
package com.stanzaliving.user.acl.service;

/**
 * @author naveen.kumar
 *
 * @date 23-Oct-2019
 *
 **/
public interface AclService {

	boolean isAccesible(String userId, String url);
}