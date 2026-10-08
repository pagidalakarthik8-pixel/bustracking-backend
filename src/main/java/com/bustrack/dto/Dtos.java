package com.bustrack.dto;

import com.bustrack.model.BusStatus;
import com.bustrack.model.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalTime;
import java.util.List;

/** Request / response payloads used by the REST controllers. */
public final class Dtos {
    private Dtos() {
    }

    // ---------- auth ----------
    public record RegisterRequest(
            @NotBlank String name,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 6, message = "must be at least 6 characters") String password,
            String rollNumber,
            String phone,
            Long busId,
            String boardingStop) {
    }

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {
    }

    public record AuthResponse(String token, UserResponse user) {
    }

    public record UserResponse(Long id, String name, String email, String role, String rollNumber,
                               String phone, Long busId, String busNumber, String boardingStop) {
        public static UserResponse from(User u) {
            return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole().name(),
                    u.getRollNumber(), u.getPhone(),
                    u.getBus() == null ? null : u.getBus().getId(),
                    u.getBus() == null ? null : u.getBus().getBusNumber(),
                    u.getBoardingStop());
        }
    }

    public record ProfileRequest(@NotBlank String name, String phone, String rollNumber, Long busId,
                                 String boardingStop) {
    }

    public record AssignBusRequest(Long busId, String boardingStop) {
    }

    // ---------- transport ----------
    public record BusRequest(
            @NotBlank String busNumber,
            @NotBlank String registrationNumber,
            @NotNull @Min(1) Integer capacity,
            Long driverId,
            Long routeId) {
    }

    public record BusStatusRequest(@NotNull BusStatus status, String statusNote) {
    }

    public record BusLocationRequest(
            @NotNull @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") Double latitude,
            @NotNull @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0") Double longitude) {
    }

    public record DriverRequest(@NotBlank String name, @NotBlank String phone, @NotBlank String licenseNumber) {
    }

    public record StopRequest(@NotBlank String name, @NotNull Integer stopOrder, LocalTime pickupTime) {
    }

    public record RouteRequest(
            @NotBlank String name,
            @NotBlank String startPoint,
            @NotBlank String endPoint,
            Double distanceKm,
            @Valid List<StopRequest> stops) {
    }

    public record ScheduleRequest(
            @NotNull Long busId,
            @NotNull LocalTime departureTime,
            @NotNull LocalTime arrivalTime,
            @NotBlank String trip,
            @NotBlank String days) {
    }

    public record NotificationRequest(@NotBlank String title, @NotBlank @Size(max = 1000) String message,
                                      Long busId) {
    }
}
