/**
 * 
 */
package com.stanzaliving.user.entity;

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

@Getter
@Setter
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "blacklist_user")
@Entity(name = "blacklist_user")
public class BlacklistUserEntity extends AbstractJpaEntity {

	private static final long serialVersionUID = 2284651697599647979L;

	@Column(name = "mobile", columnDefinition = "varchar(15)")
	private String mobile;

}