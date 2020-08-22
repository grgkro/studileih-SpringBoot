package com.example.studileih.Service;

import com.example.studileih.Dto.ChatDto;
import com.example.studileih.Dto.MessageDto;
import com.example.studileih.Entity.Chat;
import com.example.studileih.Entity.Message;
import com.example.studileih.Entity.Product;
import com.example.studileih.Entity.User;
import com.example.studileih.Repository.ChatRepository;
import com.example.studileih.Repository.MessageRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Service;

import javax.management.Query;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ModelMapper modelMapper;  //modelMapper konvertiert Entities in DTOs (modelMapper Dependency muss in pom.xml drin sein)

    public void saveOrUpdateChat(Chat chat) {
        chatRepository.save(chat);
    }

    public List<Chat> loadAllChat() {
        // get all chats from DB
        List<Chat> chats = new ArrayList<>();
        chatRepository.findAll().forEach(chats::add); // users::add ist gleich wie: users.add(user)
        return chats;
    }

    /**
     * Converts a Chat to a ChatDto.
     *
     * @param chat
     * @return chatDto
     */
    private ChatDto convertChatToDto(Chat chat) {
        ChatDto chatDto = modelMapper.map(chat, ChatDto.class);
        return chatDto;
    }

    /**
     * Converts many Chats to ChatDtos.
     */
    public List<ChatDto> convertChatsToDtos(List<Chat> chats) {
       return chats.stream()
               .map(this::convertChatToDto)
               .collect(Collectors.toList());
    }

    public List<ChatDto> findChatsByUserId(Long id) {
            return convertChatsToDtos(chatRepository.findChatsByUserId(String.valueOf(id)));
    }

    public Optional<Chat> getChatById(Long id) {
        return chatRepository.findById(id);
    }
}