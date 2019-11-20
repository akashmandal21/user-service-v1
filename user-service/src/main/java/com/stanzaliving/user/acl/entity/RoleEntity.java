/**
 * 
 */
package com.stanzaliving.user.acl.entity;

import javax.persistence.*;

import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.sqljpa.entity.AbstractJpaEntity;

import com.stanzaliving.core.base.enums.AccessLevel;
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
@Table(name = "roles", uniqueConstraints = {@UniqueConstraint(name = "UK_role_department_accesslevel", columnNames = {"role_name", "department", "access_level"})})
@Entity(name = "roles")
public class RoleEntity extends AbstractJpaEntity {

	private static final long serialVersionUID = 7105880327634827863L;

	@Column(name = "role_name", columnDefinition = "varchar(255) NOT NULL", unique = true)
	private String roleName;

	@Enumerated(EnumType.STRING)
	@Column(name = "department", columnDefinition = "varchar(30)", nullable = false)
	private Department department;

	@Enumerated(EnumType.STRING)
	@Column(name = "access_level", columnDefinition = "varchar(30)", nullable = false)
	private AccessLevel accessLevel;



}