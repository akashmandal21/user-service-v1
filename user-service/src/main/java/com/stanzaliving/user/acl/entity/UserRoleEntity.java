/**
 * 
 */
package com.stanzaliving.user.acl.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.stanzaliving.core.sqljpa.entity.AbstractJpaEntity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * @author naveen
 *
 * @date 20-Oct-2019
 */
@Getter
@Setter
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(
		name = "user_roles",
		uniqueConstraints = { @UniqueConstraint(name = "UK_user_roles_userid_roleid", columnNames = { "user_id", "role_id" }) })
@Entity(name = "user_roles")
public class UserRoleEntity extends AbstractJpaEntity {

	private static final long serialVersionUID = 5062512483145481979L;

	@Column(name = "user_id", columnDefinition = "char(40) NOT NULL")
	private String userId;

	@Column(name = "role_id", columnDefinition = "char(40) NOT NULL")
	private String roleId;

}