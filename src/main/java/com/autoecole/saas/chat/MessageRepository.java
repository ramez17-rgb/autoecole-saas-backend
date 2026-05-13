package com.autoecole.saas.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    @Query("SELECT m FROM Message m WHERE " +
           "(m.senderId = :userId AND m.receiverId = :otherId) OR " +
           "(m.senderId = :otherId AND m.receiverId = :userId) " +
           "ORDER BY m.sentAt ASC")
    List<Message> findConversation(@Param("userId") UUID userId, @Param("otherId") UUID otherId);

    @Query("SELECT m FROM Message m WHERE m.receiverId = :userId AND m.read = false")
    List<Message> findUnreadByReceiver(@Param("userId") UUID userId);

    @Query("SELECT DISTINCT CASE WHEN m.senderId = :userId THEN m.receiverId ELSE m.senderId END " +
           "FROM Message m WHERE m.senderId = :userId OR m.receiverId = :userId")
    List<UUID> findContactIds(@Param("userId") UUID userId);
}
