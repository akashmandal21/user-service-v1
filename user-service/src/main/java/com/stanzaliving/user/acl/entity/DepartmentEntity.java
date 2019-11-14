/**
 * 
 */
package com.stanzaliving.user.acl.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.IdClass;
import javax.persistence.Table;

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
 * @date 19-Oct-2019
 */
@Getter
@Setter
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(RoleApiId.class)
@Table(name = "departments")
@Entity(name = "departments")
public class DepartmentEntity extends AbstractJpaEntity {

	private static final long serialVersionUID = 6314654027755027392L;

	@Column(name = "department_name", columnDefinition = "char(100)", nullable = false, unique = true)
	private String departmentName;

}