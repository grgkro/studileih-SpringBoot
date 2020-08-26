package com.example.studileih.Service;

import com.example.studileih.Dto.ChatDto;
import com.example.studileih.Dto.ChatDtoForResponding;
import com.example.studileih.Dto.MessageDto;
import com.example.studileih.Dto.MessageDtoForResponding;
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

    /**
     * Converts many Messages to MessageDtos.
     */
    public List<MessageDtoForResponding> convertMessagesToDtos(List<Message> messages) {
        return messages.stream()
                .map(this::convertMessageToDto)
                .collect(Collectors.toList());
    }

    /**
     * Converts a Messages to a MessagesDto.
     */
    private MessageDtoForResponding convertMessageToDto(Message message) {
        MessageDtoForResponding messageDto = modelMapper.map(message, MessageDtoForResponding.class);
        // modelmapper uses the toString method for the two convertion from User to String -> we have to overwrite the User strings, otherwise the encrypted passwords would be in that strings.
        messageDto.setSender(message.getSender().getName());
        messageDto.setReceiver(message.getReceiver().getName());
        return messageDto;
    }


    public List<ChatDto> findChatsByUserId(Long id) {
            return convertChatsToDtos(chatRepository.findChatsByUserId(String.valueOf(id)));
    }

    public List<MessageDtoForResponding> getAllMessagesByChatId( Long chatId) {
        Optional<Chat> chat = chatRepository.findById(chatId);
        if (chat.isPresent()) {
            Chat chat1 = chat.get();
            return convertMessagesToDtos(chat1.getMessages());
        }
        return null;
    }

    public Optional<Chat> getChatById(Long id) {
        return chatRepository.findById(id);
    }

    public ChatDtoForResponding getChatDtoForRespondingById(Long id) {
        Optional<Chat> chat = getChatById(id);
        if (chat.isPresent()) {
            Chat chat1 = chat.get();
            return new ChatDtoForResponding(chat1.getId(), chat1.getUser1().getName(), chat1.getUser2().getName(), getAllMessagesByChatId(chat1.getId()));
        }
        return null;
    }

}