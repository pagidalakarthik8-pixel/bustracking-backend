package com.bustrack.service;

import com.bustrack.model.*;
import com.bustrack.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Builds the personalised dashboard payloads for students and administrators. */
@Service
public class DashboardService {

    private final BusRepository busRepository;
    private final DriverRepository driverRepository;
    private final RouteRepository routeRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public DashboardService(BusRepository busRepository, DriverRepository driverRepository,
                            RouteRepository routeRepository, ScheduleRepository scheduleRepository,
                            UserRepository userRepository, NotificationService notificationService) {
        this.busRepository = busRepository;
        this.driverRepository = driverRepository;
        this.routeRepository = routeRepository;
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> build(User user) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("role", user.getRole().name());

        Map<String, Long> statusCounts = new LinkedHashMap<>();
        for (BusStatus s : BusStatus.values()) {
            statusCounts.put(s.name(), busRepository.countByStatus(s));
        }
        out.put("statusCounts", statusCounts);
        List<Notification> notifications = notificationService.findForUser(user);
        out.put("notifications", notifications.stream().limit(5).toList());

        if (user.getRole() == Role.ADMIN) {
            Map<String, Long> totals = new LinkedHashMap<>();
            totals.put("buses", busRepository.count());
            totals.put("drivers", driverRepository.count());
            totals.put("routes", routeRepository.count());
            totals.put("schedules", scheduleRepository.count());
            totals.put("students", userRepository.countByRole(Role.STUDENT));
            out.put("totals", totals);
            out.put("buses", busRepository.findAllByOrderByBusNumberAsc());
        } else {
            Bus bus = user.getBus();
            out.put("bus", bus);
            out.put("boardingStop", user.getBoardingStop());
            out.put("schedules", bus == null ? List.of()
                    : scheduleRepository.findByBusIdOrderByDepartureTimeAsc(bus.getId()));
            out.put("today", LocalDate.now().getDayOfWeek().name().substring(0, 3));
        }
        return out;
    }
}
