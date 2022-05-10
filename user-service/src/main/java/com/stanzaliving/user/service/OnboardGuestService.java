package com.stanzaliving.user.service;
import com.stanzaliving.booking.dto.BookingResponseDto;
import org.springframework.stereotype.Service;
@Service
public interface OnboardGuestService {
	  public BookingResponseDto createGuestBooking(String phoneNumber);

}
