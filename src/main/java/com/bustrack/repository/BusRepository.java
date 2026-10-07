package com.bustrack.repository;

import com.bustrack.model.Bus;
import com.bustrack.model.BusStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BusRepository extends JpaRepository<Bus, Long> {
    boolean existsByBusNumber(String busNumber);

    boolean existsByBusNumberAndIdNot(String busNumber, Long id);

    List<Bus> findAllByOrderByBusNumberAsc();

    long countByStatus(BusStatus status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Bus b set b.driver = null where b.driver.id = :driverId")
    void clearDriver(@Param("driverId") Long driverId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Bus b set b.route = null where b.route.id = :routeId")
    void clearRoute(@Param("routeId") Long routeId);
}
