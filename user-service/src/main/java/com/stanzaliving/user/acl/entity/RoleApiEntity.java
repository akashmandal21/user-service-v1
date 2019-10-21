/**
 * 
 */
package com.stanzaliving.user.acl.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * @author naveen
 *
 * @date 19-Oct-2019
 */
@Getter
@Setter
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(RoleApiId.class)
@Table(name = "role_api")
@Entity(name = "role_api")
public class RoleApiEntity {

	@Id
	@Column(name = "role_id")
	private long roleId;

	@Id
	@Column(name = "api_id")
	private long apiId;
}