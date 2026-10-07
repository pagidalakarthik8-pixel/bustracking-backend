package com.bustrack.service;

import com.bustrack.dto.Dtos.ScheduleRequest;
import com.bustrack.exception.BadRequestException;
import com.bustrack.exception.ResourceNotFoundException;
import com.bustrack.model.Bus;
import com.bustrack.model.Schedule;
import com.bustrack.repository.BusRepository;
import com.bustrack.repository.ScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

    private static final Set<String> TRIPS = Set.of("MORNING", "EVENING");
    private static final Set<String> DAYS = Set.of("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN");

    private final ScheduleRepository scheduleRepository;
    private final BusRepository busRepository;

    public ScheduleService(ScheduleRepository scheduleRepository, BusRepository busRepository) {
        this.scheduleRepository = scheduleRepository;
        this.busRepository = busRepository;
    }

    @Transactional(readOnly = true)
    public List<Schedule> find(Long busId) {
        return busId == null
                ? scheduleRepository.findAllByOrderByDepartureTimeAsc()
                : scheduleRepository.findByBusIdOrderByDepartureTimeAsc(busId);
    }

    @Transactional
    public Schedule create(ScheduleRequest req) {
        Schedule s = new Schedule();
        apply(s, req);
        return scheduleRepository.save(s);
    }

    @Transactional
    public Schedule update(Long id, ScheduleRequest req) {
        Schedule s = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));
        apply(s, req);
        return scheduleRepository.save(s);
    }

    @Transactional
    public void delete(Long id) {
        if (!scheduleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Schedule not found");
        }
        scheduleRepository.deleteById(id);
    }

    private void apply(Schedule s, ScheduleRequest req) {
        Bus bus = busRepository.findById(req.busId())
                .orElseThrow(() -> new BadRequestException("Selected bus does not exist"));
        String trip = req.trip().trim().toUpperCase();
        if (!TRIPS.contains(trip)) {
            throw new BadRequestException("trip must be MORNING or EVENING");
        }
        String days = Arrays.stream(req.days().split(","))
                .map(d -> d.trim().toUpperCase())
                .filter(d -> !d.isEmpty())
                .distinct()
                .collect(Collectors.joining(","));
        if (days.isEmpty() || Arrays.stream(days.split(",")).anyMatch(d -> !DAYS.contains(d))) {
            throw new BadRequestException("days must be a comma separated list of MON,TUE,WED,THU,FRI,SAT,SUN");
        }
        s.setBus(bus);
        s.setTrip(trip);
        s.setDays(days);
        s.setDepartureTime(req.departureTime());
        s.setArrivalTime(req.arrivalTime());
    }
}
