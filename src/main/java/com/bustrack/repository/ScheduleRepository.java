package com.bustrack.repository;

import com.bustrack.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findAllByOrderByDepartureTimeAsc();

    List<Schedule> findByBusIdOrderByDepartureTimeAsc(Long busId);

    void deleteByBusId(Long busId);
}
