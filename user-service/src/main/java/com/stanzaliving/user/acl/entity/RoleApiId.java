package com.stanzaliving.user.acl.entity;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RoleApiId implements Serializable {

	private static final long serialVersionUID = 1L;

	private long roleId;
	
	private long apiId;

}