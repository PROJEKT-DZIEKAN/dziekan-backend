package com.pbs.app.services;

import com.pbs.app.models.Notification;

import java.util.List;

public interface NotificationService {
    List<Notification> getAllNotificationsbyUserId(Long userId);

    List<Notification> getAllByUserIdAndIsReadFalse(Long userId);

    List<Notification> getAllByUserIdOrderByCreatedAtDesc(Long userId);

    long countUnreadNotificationsByUserId(Long userId);

    Notification createNotification(Notification notification);

    Notification markNotificationAsRead(Long notificationId);

    void deleteNotification(Long notificationId);

}