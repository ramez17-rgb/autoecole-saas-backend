package com.autoecole.saas.notification;

import com.autoecole.saas.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Get all notifications for the current Gérant's tenant.
     */
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getNotifications(@AuthenticationPrincipal User user) {
        UUID tenantId = getTenantId(user);
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        List<NotificationDTO> dtos = notificationRepository
                .findByTenantIdOrderByCreatedAtDesc(tenantId)
                .stream()
                .map(NotificationDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Get unread notification count for the current Gérant's tenant.
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal User user) {
        UUID tenantId = getTenantId(user);
        if (tenantId == null) {
            return ResponseEntity.ok(Map.of("count", 0L));
        }
        long count = notificationRepository.countByTenantIdAndReadFalse(tenantId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * Mark a notification as read.
     */
    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        UUID tenantId = getTenantId(user);
        java.util.Optional<Notification> opt = notificationRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.<Void>notFound().build();
        }
        Notification notification = opt.get();
        if (tenantId == null || !notification.getTenantId().equals(tenantId)) {
            return ResponseEntity.status(403).<Void>build();
        }
        notification.setRead(true);
        notificationRepository.save(notification);
        return ResponseEntity.<Void>ok().build();
    }

    /**
     * Mark all notifications as read for the current tenant.
     */
    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal User user) {
        UUID tenantId = getTenantId(user);
        if (tenantId == null) return ResponseEntity.badRequest().build();

        List<Notification> unread = notificationRepository.findByTenantIdOrderByCreatedAtDesc(tenantId)
                .stream()
                .filter(n -> !n.isRead())
                .collect(Collectors.toList());
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
        return ResponseEntity.<Void>ok().build();
    }

    private UUID getTenantId(User user) {
        if (user == null || user.getTenant() == null) return null;
        return user.getTenant().getId();
    }
}
