package com.pbs.app.controllers;

import com.pbs.app.models.Notification;
import com.pbs.app.repositories.UserRepository;
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
    private final UserRepository userRepository;


    @PostMapping("/create")
    public ResponseEntity<Notification> createNotification(@RequestBody Notification notification) {
        if (notification.getUsers() == null || notification.getUsers().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<Long> userIds = notification.getUsers().stream()
                .map(User::getId)
                .toList();

        Notification saved = notificationService.createNotification(notification);

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
    public ResponseEntity<Notification> broadcastNotification(@RequestBody Notification notification) {
        List<User> allUsers = userRepository.findAll();
        notification.setUsers(allUsers);
        Notification saved = notificationService.createNotification(notification);
        for (User user : allUsers) {
            messagingTemplate.convertAndSendToUser(
                    user.getId().toString(),
                    "/queue/notifications",
                    saved
            );
        }

        messagingTemplate.convertAndSend("/topic/notifications", saved);

        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<String> deleteNotification(@PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId);
        messagingTemplate.convertAndSend("/topic/notification-deleted", notificationId.toString());

        return ResponseEntity.ok("Notification deleted");
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<String> markAsRead(@PathVariable Long notificationId) {
        notificationService.markNotificationAsRead(notificationId);
        messagingTemplate.convertAndSend("/topic/notification-read", notificationId.toString());

        return ResponseEntity.ok("Notification marked as read");
    }

    @GetMapping()
    public List<Notification> getNotifications(@RequestParam Long userId) {
        return notificationService.getAllNotificationsbyUserId(userId);
    }

    @GetMapping("/unread")
    public List<Notification> getUnreadNotifications(@RequestParam Long userId) {
        return notificationService.getAllByUserIdAndIsReadFalse(userId);
    }

    @GetMapping("/newest")
    public List<Notification> getNewestNotifications(@RequestParam Long userId)
    {
        return notificationService.getAllByUserIdOrderByCreatedAtDesc(userId);
    }
}