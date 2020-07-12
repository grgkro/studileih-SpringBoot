package com.example.studileih.Repository;

import com.example.studileih.Entity.Chat;
import com.example.studileih.Entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
}
