/**
 * 
 */
package com.stanzaliving.user.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author naveen
 *
 * @date 11-Oct-2019
 */
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class AddressEntity implements Serializable {

	private static final long serialVersionUID = 2609440194410033833L;

	@Column(name = "address_line_1", columnDefinition = "TEXT")
	private String addressLine1;

	@Column(name = "address_line_2", columnDefinition = "TEXT")
	private String addressLine2;

	@Column(name = "landmark", columnDefinition = "TEXT")
	private String landmark;

	@Column(name = "city", columnDefinition = "varchar(100)")
	private String cityName;

	@Column(name = "state", columnDefinition = "varchar(100)")
	private String stateName;

	@Column(name = "postal_code", columnDefinition = "varchar(20)")
	private String postalCode;

	@Column(name = "country", columnDefinition = "varchar(100)")
	private String countryName;

}