package com.example.studileih.Repository;

import com.example.studileih.Entity.Chat;
import com.example.studileih.Entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    //dangerous because of SQL Injection?
    @Query(value = "SELECT * FROM chats WHERE user1_id = ?1 OR user2_id = ?1", nativeQuery = true)
    List<Chat> findChatsByUserId(String id);

}
