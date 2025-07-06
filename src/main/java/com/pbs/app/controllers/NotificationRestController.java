package com.pbs.app.controllers;

import com.pbs.app.models.Notification;
import com.pbs.app.services.NotificationServiceImpl;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import com.pbs.app.models.User;
import java.util.stream.Collectors;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationRestController {

    private static final Logger log =
            LoggerFactory.getLogger(NotificationRestController.class);

    private final NotificationServiceImpl notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/send/{userId}")
    public void sendNotification(@PathVariable Long userId, @RequestBody String message) {
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                message
        );
    }

    @PostMapping
    public ResponseEntity<Notification> createNotification(@RequestBody Notification notification) {
        List<Long> userIds = notification.getUsers().stream()
                .map(User::getId)
                .collect(Collectors.toList());
        log.info("REST createNotification for users {}", userIds);

        Notification saved = notificationService.createNotification(notification);

        // Wyślij powiadomienie przez WebSocket do każdego użytkownika
        for (Long userId : userIds) {
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/notifications",
                    saved
            );
        }

        return ResponseEntity.ok(saved);
    }


    @PostMapping("/broadcast")
    public void broadcastNotification(@RequestBody String message) {
        messagingTemplate.convertAndSend("/topic/notifications", message);
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<String> deleteNotification(@PathVariable Long notificationId) {
        log.info("REST deleteNotification: {}", notificationId);

        notificationService.deleteNotification(notificationId);
        messagingTemplate.convertAndSend("/topic/notification-deleted", notificationId.toString());

        return ResponseEntity.ok("Notification deleted");
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<String> markAsRead(@PathVariable Long notificationId) {
        log.info("REST markAsRead: {}", notificationId);

        notificationService.markNotificationAsRead(notificationId);
        messagingTemplate.convertAndSend("/topic/notification-read", notificationId.toString());

        return ResponseEntity.ok("Notification marked as read");
    }

    @GetMapping()
    public List<Notification> getNotifications(@RequestParam Long userId) {
        log.info("REST getNotifications for user {}", userId);
        return notificationService.getAllNotificationsbyUserId(userId);
    }

    @GetMapping("/unread")
    public List<Notification> getUnreadNotifications(@RequestParam Long userId) {
        log.info("REST getUnreadNotifications for user {}", userId);
        return notificationService.getAllByUserIdAndIsReadFalse(userId);
    }

    @GetMapping("/newest")
    public List<Notification> getNewestNotifications(@RequestParam Long userId)
    {
        log.info("REST getNewestNotifications for user {}", userId);
        return notificationService.getAllByUserIdOrderByCreatedAtDesc(userId);
    }
}