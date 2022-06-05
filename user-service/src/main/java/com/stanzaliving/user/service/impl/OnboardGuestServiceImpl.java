package com.stanzaliving.user.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stanzaliving.booking.dto.BookingResponseDto;
import com.stanzaliving.user.controller.AuthController;
import com.stanzaliving.user.service.OnboardGuestService;

import lombok.extern.log4j.Log4j2;

import com.stanzaliving.core.client.api.BookingDataControllerApi;


@Service
@Log4j2
public class OnboardGuestServiceImpl implements OnboardGuestService {

    @Autowired
    private BookingDataControllerApi bookingDataControllerApi;

    public BookingResponseDto createGuestBooking(String phoneNumber) {
        log.info("Inside createGuestBooking with phoneNumber {} " + phoneNumber);
        BookingResponseDto bookingResponseDto=null;
        try {
            bookingResponseDto = bookingDataControllerApi.createGuestBooking(phoneNumber);

        } catch (Exception e) {
            log.error("Error in createGuestBooking for phoneNumber : {} is: {}", phoneNumber, e.getMessage());
        }

        return bookingResponseDto;
    }

}