/**
 * 
 */
package com.stanzaliving.user.acl.entity;

import javax.persistence.*;

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
@Table(name = "role_api", uniqueConstraints = { @UniqueConstraint(name = "UK_roleUuid_apiUuid ", columnNames = { "role_uuid", "api_uuid" }) })
@Entity(name = "role_api")
public class RoleAssignmentEntity extends AbstractJpaEntity {

	private static final long serialVersionUID = 5062512483145481979L;

	@Column(name = "role_uuid", columnDefinition = "char(40) NOT NULL")
	private long roleUuid;

	@Column(name = "api_uuid", columnDefinition = "char(40) NOT NULL")
	private long assignedUuid;

	@Enumerated
	@Column(name = "assignment_type", columnDefinition = "char(40) NOT NULL")
}