package com.example.studileih.Repository;

import com.example.studileih.Entity.Dorm;
import com.example.studileih.Entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
}
