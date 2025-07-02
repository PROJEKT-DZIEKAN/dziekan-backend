package com.pbs.app.repositories;

import com.pbs.app.models.Chat;
import com.pbs.app.models.Message;
import com.pbs.app.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat,Long> {
//    List<Chat> findByUserA(User userA);
//    List<Chat> findByUserB(User userB);
//    Optional<Chat> findByUserAAndUserB(User userA, User userB);
//    boolean existsByUserAAndUserB(User userA, User userB);
//
//    @Query("SELECT COUNT(m) FROM Message m WHERE m.chat.id = :chatId")
//    int countMessagesInChat(@Param("chatId") Long chatId);
//
//    @Query("SELECT c FROM Chat c WHERE c.userA = :user OR c.userB = :user")
//    List<Chat> findByUser(@Param("user") User user);
//
//    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId ORDER BY m.sentAt DESC")
//    Optional<Message> getLastMessage(@Param("chatId") Long chatId);


    //to wyzej jest ale nie potrzebne wiec zakomentowane

    Optional<Chat> findByUserA_IdAndUserB_Id(Long a, Long b);
    //Optional<Chat> findByUserA_IdAndUserB_Id(Long b, Long a);

    List<Chat> findByUserA_IdOrUserB_Id(Long userAId, Long userBId);

}
