package com.bustrack.repository;

import com.bustrack.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByOrderByCreatedAtDesc();

    /** General announcements plus those targeted at the given bus. */
    @Query("select n from Notification n where n.bus is null or n.bus.id = :busId order by n.createdAt desc")
    List<Notification> findForBus(@Param("busId") Long busId);

    @Query("select n from Notification n where n.bus is null order by n.createdAt desc")
    List<Notification> findGeneral();

    void deleteByBusId(Long busId);
}
