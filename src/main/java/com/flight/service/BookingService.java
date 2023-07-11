package com.flight.service;

import org.springframework.stereotype.Service;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final FlightVendorApiService flightVendorApiService;
    private final CouponService couponService;

    public BookingService(BookingRepository bookingRepository, FlightVendorApiService flightVendorApiService, CouponService couponService) {
        this.bookingRepository = bookingRepository;
        this.flightVendorApiService = flightVendorApiService;
        this.couponService = couponService;
    }

    public Booking createBooking(BookingRequest bookingRequest, Long userId) {
        // Validate the booking request
        ValidationResult validationResult = validateBookingRequest(bookingRequest);
        if (!validationResult.isValid()) {
            throw new InvalidBookingException(validationResult.getErrors());
        }

        // Check seat availability with the flight vendor API
        boolean seatAvailable = flightVendorApiService.checkSeatAvailability(bookingRequest.getFlightId(), bookingRequest.getSeatCount());
        if (!seatAvailable) {
            throw new SeatNotAvailableException("Seat not available");
        }

        // Apply coupon discount if applicable
        boolean couponApplied = couponService.applyCoupon(userId);
        double bookingAmount = calculateBookingAmount(bookingRequest.getBasePrice(), bookingRequest.getSeatCount(), couponApplied);

        // Perform the booking
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setFlightId(bookingRequest.getFlightId());
        booking.setSeatCount(bookingRequest.getSeatCount());
        booking.setAmount(bookingAmount);
        booking.setStatus(BookingStatus.CONFIRMED); // Assuming the booking is confirmed for simplicity

        // Save the booking to the repository
        return bookingRepository.save(booking);
    }

    public Booking getBooking(Long bookingId) {
        return bookingRepository.findById(bookingId).orElse(null);
    }

    public void cancelBooking(Long bookingId) {
        // Retrieve the booking
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            throw new BookingNotFoundException("Booking not found");
        }

        // Check if the booking is cancellable (e.g., within a specific time limit)
        if (!isBookingCancellable(booking)) {
            throw new BookingNotCancellableException("Booking cannot be cancelled");
        }

        // Cancel the booking with the flight vendor API
        boolean cancellationSuccess = flightVendorApiService.cancelBooking(booking.getFlightId(), booking.getSeatCount());
        if (!cancellationSuccess) {
            throw new BookingCancellationException("Failed to cancel booking");
        }

        // Update the booking status
        booking.setStatus(BookingStatus.CANCELLED);

        // Save the updated booking to the repository
        bookingRepository.save(booking);
    }

    // Other methods for updating, modifying, or retrieving bookings

    private ValidationResult validateBookingRequest(BookingRequest bookingRequest) {
        ValidationResult validationResult = new ValidationResult();

        // Perform validation on the booking request
        // Add errors to the validationResult if any validation fails
        if (bookingRequest.getFlightId() == null) {
            validationResult.addError("Flight ID is required");
        }

        if (bookingRequest.getSeatCount() <= 0) {
            validationResult.addError("Seat count must be greater than zero");
        }

        // Additional validations based on your specific requirements

        return validationResult;
    }

    private double calculateBookingAmount(double basePrice, int seatCount, boolean couponApplied) {
        double totalAmount = basePrice * seatCount;
        if (couponApplied) {
            double discount = totalAmount * 0.15; // Assuming a 15% discount for simplicity
            return totalAmount - discount;
        }
        return totalAmount;
    }

    private boolean isBookingCancellable(Booking booking) {
        // Implement logic to check if the booking is cancellable (e.g., within a specific time limit)
        return true; // Return true if the booking is cancellable; otherwise, return false
    }
}
