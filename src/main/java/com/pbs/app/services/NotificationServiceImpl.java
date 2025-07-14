package com.pbs.app.services;

import com.pbs.app.models.Notification;
import com.pbs.app.repositories.NotificationRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService{
    private final NotificationRepository notificationRepository;
    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public List<Notification> getAllNotificationsbyUserId(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional
    public List<Notification> getAllByUserIdOrderByCreatedAtDesc(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional
    public List<Notification> getAllByUserIdAndIsReadFalse(Long userId)
    {
        return notificationRepository.findByIsReadFalseAndUserId(userId);
    }

    @Override
    @Transactional
    public long countUnreadNotificationsByUserId(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public Notification createNotification(Notification notification)
    {
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void deleteNotification(Long id) {
        notificationRepository.deleteNotificationById(id);
    }

    @Override
    @Transactional
    public Notification markNotificationAsRead(Long id) {
        notificationRepository.markNotificationAsRead(id);
        return notificationRepository.findById(id).orElse(null);
    }
}
