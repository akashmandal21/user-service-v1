/**
 * 
 */
package com.stanzaliving.user.acl.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
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
@Table(name = "api")
@Entity(name = "api")
public class ApiEntity extends AbstractJpaEntity {

	private static final long serialVersionUID = 5062512483145481979L;

	@Column(name = "api_name", columnDefinition = "varchar(255) NOT NULL")
	private String apiName;

	@Column(name = "action_url", columnDefinition = "varchar(255) NOT NULL", unique = true)
	private String actionUrl;

	@Column(name = "category", columnDefinition = "varchar(100) NOT NULL")
	private String category;

}