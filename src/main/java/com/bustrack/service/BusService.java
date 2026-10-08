package com.bustrack.service;

import com.bustrack.dto.Dtos.BusRequest;
import com.bustrack.dto.Dtos.BusStatusRequest;
import com.bustrack.exception.BadRequestException;
import com.bustrack.exception.ResourceNotFoundException;
import com.bustrack.model.Bus;
import com.bustrack.model.BusStatus;
import com.bustrack.model.Driver;
import com.bustrack.model.Route;
import com.bustrack.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class BusService {

    private final BusRepository busRepository;
    private final DriverRepository driverRepository;
    private final RouteRepository routeRepository;
    private final ScheduleRepository scheduleRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public BusService(BusRepository busRepository, DriverRepository driverRepository,
                      RouteRepository routeRepository, ScheduleRepository scheduleRepository,
                      NotificationRepository notificationRepository, UserRepository userRepository,
                      NotificationService notificationService) {
        this.busRepository = busRepository;
        this.driverRepository = driverRepository;
        this.routeRepository = routeRepository;
        this.scheduleRepository = scheduleRepository;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<Bus> findAll() {
        return busRepository.findAllByOrderByBusNumberAsc();
    }

    @Transactional(readOnly = true)
    public Bus get(Long id) {
        return busRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Bus not found"));
    }

    @Transactional
    public Bus create(BusRequest req) {
        if (busRepository.existsByBusNumber(req.busNumber().trim())) {
            throw new BadRequestException("A bus with this number already exists");
        }
        Bus bus = new Bus();
        bus.setStatus(BusStatus.ON_TIME);
        apply(bus, req);
        return busRepository.save(bus);
    }

    @Transactional
    public Bus update(Long id, BusRequest req) {
        Bus bus = get(id);
        if (busRepository.existsByBusNumberAndIdNot(req.busNumber().trim(), id)) {
            throw new BadRequestException("A bus with this number already exists");
        }
        apply(bus, req);
        return busRepository.save(bus);
    }

    /** Updates the live status and tells the students on that bus when something is not normal. */
    @Transactional
    public Bus updateStatus(Long id, BusStatusRequest req) {
        Bus bus = get(id);
        BusStatus previous = bus.getStatus();
        bus.setStatus(req.status());
        bus.setStatusNote(req.statusNote() == null || req.statusNote().isBlank() ? null : req.statusNote().trim());
        Bus saved = busRepository.save(bus);
        if (previous != req.status()) {
            String label = req.status().name().replace('_', ' ').toLowerCase();
            String message = "Bus " + saved.getBusNumber() + " is now " + label
                    + (saved.getStatusNote() != null ? ": " + saved.getStatusNote() : ".");
            notificationService.create("Bus " + saved.getBusNumber() + " status update", message, saved);
        }
        return saved;
    }

    @Transactional
    public Bus updateLocation(Long id, Double latitude, Double longitude) {
        Bus bus = get(id);
        bus.setLatitude(latitude);
        bus.setLongitude(longitude);
        bus.setLocationUpdatedAt(Instant.now());
        return busRepository.save(bus);
    }

    @Transactional
    public void delete(Long id) {
        Bus bus = get(id);
        userRepository.clearBus(id);
        scheduleRepository.deleteByBusId(id);
        notificationRepository.deleteByBusId(id);
        busRepository.delete(bus);
    }

    private void apply(Bus bus, BusRequest req) {
        bus.setBusNumber(req.busNumber().trim());
        bus.setRegistrationNumber(req.registrationNumber().trim());
        bus.setCapacity(req.capacity());
        Driver driver = null;
        if (req.driverId() != null) {
            driver = driverRepository.findById(req.driverId())
                    .orElseThrow(() -> new BadRequestException("Selected driver does not exist"));
        }
        Route route = null;
        if (req.routeId() != null) {
            route = routeRepository.findById(req.routeId())
                    .orElseThrow(() -> new BadRequestException("Selected route does not exist"));
        }
        bus.setDriver(driver);
        bus.setRoute(route);
    }
}
