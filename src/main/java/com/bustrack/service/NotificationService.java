package com.bustrack.service;

import com.bustrack.dto.Dtos.NotificationRequest;
import com.bustrack.exception.BadRequestException;
import com.bustrack.exception.ResourceNotFoundException;
import com.bustrack.model.Bus;
import com.bustrack.model.Notification;
import com.bustrack.model.Role;
import com.bustrack.model.User;
import com.bustrack.repository.BusRepository;
import com.bustrack.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final BusRepository busRepository;

    public NotificationService(NotificationRepository notificationRepository, BusRepository busRepository) {
        this.notificationRepository = notificationRepository;
        this.busRepository = busRepository;
    }

    /** Admins see everything; students see general announcements and those for their own bus. */
    @Transactional(readOnly = true)
    public List<Notification> findForUser(User user) {
        if (user.getRole() == Role.ADMIN) {
            return notificationRepository.findAllByOrderByCreatedAtDesc();
        }
        if (user.getBus() == null) {
            return notificationRepository.findGeneral();
        }
        return notificationRepository.findForBus(user.getBus().getId());
    }

    @Transactional
    public Notification create(NotificationRequest req) {
        Bus bus = null;
        if (req.busId() != null) {
            bus = busRepository.findById(req.busId())
                    .orElseThrow(() -> new BadRequestException("Selected bus does not exist"));
        }
        return create(req.title().trim(), req.message().trim(), bus);
    }

    @Transactional
    public Notification create(String title, String message, Bus bus) {
        Notification n = new Notification();
        n.setTitle(title);
        n.setMessage(message);
        n.setBus(bus);
        n.setCreatedAt(LocalDateTime.now());
        return notificationRepository.save(n);
    }

    @Transactional
    public void delete(Long id) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notificationRepository.delete(n);
    }
}
