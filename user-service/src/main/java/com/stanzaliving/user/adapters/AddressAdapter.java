/**
 * 
 */
package com.stanzaliving.user.adapters;

import java.util.Objects;

import com.stanzaliving.core.user.dto.Address;
import com.stanzaliving.user.entity.AddressEntity;

import lombok.experimental.UtilityClass;

/**
 * @author naveen
 *
 * @date 11-Oct-2019
 */
@UtilityClass
public class AddressAdapter {

	public static AddressEntity getAddressEntity(Address address) {

		if (Objects.isNull(address)) {
			return null;
		}

		return AddressEntity.builder()
				.addressLine1(address.getAddressLine1())
				.addressLine2(address.getAddressLine2())
				.landmark(address.getLandmark())
				.cityName(address.getCityName())
				.stateName(address.getStateName())
				.postalCode(address.getPostalCode())
				.countryName(address.getCountryName())
				.build();
	}

	public static Address getAddressDto(AddressEntity address) {

		if (Objects.isNull(address)) {
			return null;
		}

		return Address.builder()
				.addressLine1(address.getAddressLine1())
				.addressLine2(address.getAddressLine2())
				.landmark(address.getLandmark())
				.cityName(address.getCityName())
				.stateName(address.getStateName())
				.postalCode(address.getPostalCode())
				.countryName(address.getCountryName())
				.build();
	}

}