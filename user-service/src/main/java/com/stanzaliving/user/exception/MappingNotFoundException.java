/**
 * 
 */
package com.stanzaliving.user.exception;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author naveen
 *
 * @date 13-Oct-2019
 */
@Getter
@Setter
@ToString(callSuper = true)
public class MappingNotFoundException extends RuntimeException {

	private static final long serialVersionUID = -5511995135010774989L;

	private final String message;

	private final String errorCode;

	public MappingNotFoundException(String message) {
		super(message);
		this.message = message;
		this.errorCode = null;
	}

	public MappingNotFoundException(String message, String errorCode) {
		super(message);
		this.message = message;
		this.errorCode = errorCode;
	}
}