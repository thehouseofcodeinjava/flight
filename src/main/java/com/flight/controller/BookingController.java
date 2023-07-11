package com.flight.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final UserService userService;
    private final PaymentGatewayService paymentGatewayService;

    public BookingController(BookingService bookingService, UserService userService, PaymentGatewayService paymentGatewayService) {
        this.bookingService = bookingService;
        this.userService = userService;
        this.paymentGatewayService = paymentGatewayService;
    }

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest bookingRequest) {
        // Validate the booking request
        ValidationResult validationResult = validateBookingRequest(bookingRequest);
        if (!validationResult.isValid()) {
            return ResponseEntity.badRequest().body(validationResult.getErrors());
        }

        // Retrieve the authenticated user's ID (assuming user authentication is implemented)
        Long userId = getAuthenticatedUserId();

        // Check if the user has the necessary permissions
        if (!userService.hasPermission(userId, Permission.BOOK_FLIGHT)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Check seat availability with the flight vendor API
        boolean seatAvailable = bookingService.checkSeatAvailability(bookingRequest);
        if (!seatAvailable) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Seat not available");
        }

        // Make the payment using the payment gateway service
        PaymentResult paymentResult = paymentGatewayService.makePayment(bookingRequest.getPaymentDetails());
        if (!paymentResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Payment failed");
        }

        // Create the booking
        Booking booking = bookingService.createBooking(bookingRequest, userId, paymentResult.getTransactionId());

        // Generate a response
        BookingResponse response = new BookingResponse(booking.getId(), booking.getStatus());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<?> getBooking(@PathVariable("bookingId") Long bookingId) {
        // Retrieve the authenticated user's ID (assuming user authentication is implemented)
        Long userId = getAuthenticatedUserId();

        // Check if the user has the necessary permissions
        if (!userService.hasPermission(userId, Permission.READ_BOOKING)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Retrieve the booking
        Booking booking = bookingService.getBooking(bookingId);

        // Check if the booking exists
        if (booking == null) {
            return ResponseEntity.notFound().build();
        }

        // Check if the user is authorized to access the booking
        if (!booking.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Generate a response
        BookingDetailsResponse response = new BookingDetailsResponse(booking.getId(), booking.getUserId(), booking.getStatus(), booking.getFlightDetails());

        return ResponseEntity.ok(response);
    }

    // Other endpoints for updating, canceling bookings, etc.

    private ValidationResult validateBookingRequest(BookingRequest bookingRequest) {
        ValidationResult validationResult = new ValidationResult();

        // Perform validation on the booking request
        // Add errors to the validationResult if any validation fails

        return validationResult;
    }

    private Long getAuthenticatedUserId() {
        // Implement the logic to retrieve the authenticated user's ID
        // Return the user ID
    }
}

