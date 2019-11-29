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

	boolean isAccessible(String userId, String url);
}