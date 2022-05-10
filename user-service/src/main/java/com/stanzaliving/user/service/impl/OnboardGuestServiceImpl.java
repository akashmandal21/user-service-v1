package com.stanzaliving.user.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stanzaliving.booking.dto.BookingResponseDto;
import com.stanzaliving.user.service.OnboardGuestService;

import lombok.extern.log4j.Log4j2;

import com.stanzaliving.core.client.api.BookingDataControllerApi;


@Service
public class OnboardGuestServiceImpl implements OnboardGuestService {
	
	@Autowired
	private BookingDataControllerApi bookingDataControllerApi;
	    
	public BookingResponseDto createGuestBooking(String phoneNumber) {
		BookingResponseDto BookingResponseDto = bookingDataControllerApi.createGuestBooking(phoneNumber);
		
		return BookingResponseDto;
	}

}
