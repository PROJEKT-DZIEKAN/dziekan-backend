package com.pbs.app.repositories;

import com.pbs.app.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n JOIN n.users u WHERE u.id = :userId")
    List<Notification> findByUserId(@Param("userId") Long userId);

    @Query("SELECT n FROM Notification n JOIN n.users u WHERE u.id = :userId AND n.isRead = false")
    List<Notification> findByIsReadFalseAndUserId(@Param("userId") Long userId);

    @Query("SELECT n FROM Notification n JOIN n.users u WHERE u.id = :userId ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("SELECT COUNT(n) FROM Notification n JOIN n.users u WHERE u.id = :userId AND n.isRead = false")
    long countByUserIdAndIsReadFalse(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :id")
    void markNotificationAsRead(@Param("id") Long id);

    void deleteNotificationById(Long id);
}