package com.example.studileih.Service;

import com.example.studileih.Dto.MessageDto;
import com.example.studileih.Dto.UserDto;
import com.example.studileih.Entity.Chat;
import com.example.studileih.Entity.Message;
import com.example.studileih.Entity.Product;
import com.example.studileih.Entity.User;
import com.example.studileih.Repository.MessageRepository;
import com.example.studileih.Repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private ModelMapper modelMapper;  //modelMapper konvertiert Entities in DTOs (modelMapper Dependency muss in pom.xml drin sein)

    public void saveOrUpdateMessage(Message message) {
        messageRepository.save(message);
    }

    public void deleteMessage(Long id) {
       messageRepository.deleteById(id);
    }

    public List<MessageDto> loadAll() {
        // get all messages from DB
        List<Message> messages = new ArrayList<>();
        messageRepository.findAll().forEach(messages::add); // users::add ist gleich wie: users.add(user)
        // map all users to userDtos (das ummappen klappt, obwohl es im UserDto nur eine Variable "private Long dormId" gibt und im User das als richtiges Dorm drin ist (private Dorm dorm). der modelmapper scheint da einfach gut zu raten, dass er von user nur die dorm.id nehmen muss und sie in dormId im Dto kommt.
        return messages.stream()                 // List<Product> muss zu List<ProductDto> konvertiert werden. Hier tun wir zuerst die List<Product> in einen Stream umwandeln
                .map(this::convertMessageToDto)            // Dann jedes Product ausm Stream herausnehmen und zu ProductDto umwandeln
                .collect(Collectors.toList());      // und dann den neuen Stream als List<ProductDto> einsammeln.
    }

    /**
     * Converts a Message to a MessageDto.
     * @param message
     * @return userDto
     */
    private MessageDto convertMessageToDto(Message message) {
        MessageDto messageDto = modelMapper.map(message, MessageDto.class);

        return messageDto;
    }

    public ResponseEntity<String> sendMessageToOwner(String startDate, String endDate, Product product, User userWhoWantsToRent, User owner) {
        // get previous Chat between the two Users, or create new Chat
        Chat previousChat = getPreviousChat(userWhoWantsToRent, owner);
        //create Message with the given infos
        Message message = createMessage(startDate, endDate, product,userWhoWantsToRent, owner, previousChat);
        //add message to the chat
        addMessageToChat(previousChat, message);
        // save the chat
        chatService.saveOrUpdateChat(previousChat);
        // finally save the message itself (saving the chat only tells us that there is a conversation between those two users, but not what the single texts, subjects are. Thats what the Message is for.)
        //by saving the message we also update the table chat_messages (bc chat_messages is mapped by Message) that connects the chats with the messages.
        saveOrUpdateMessage(message);
        // there shouldnt be a way how this can fail, so we always return status ok: 200
        return ResponseEntity.status(HttpStatus.OK).body(owner.getName() + " erhält auf Studileih.de eine Benachrichtigung über deine Anfrage.");
    }

    //add message to the chat. If this is the first message in this chat previousChat.getMessages() would cause a NullPointerException, so we have to check if the messages != null first.
    private void addMessageToChat(Chat previousChat, Message message) {
        if (previousChat.getMessages() != null) {
            previousChat.getMessages().add(message);
        } else {
            List<Message> messages = new ArrayList<>();
            messages.add(message);
            previousChat.setMessages(messages);
        }
    }

    private Message createMessage(String startDate, String endDate, Product product, User userWhoWantsToRent, User owner, Chat previousChat) {
        String subject = "Neue Ausleianfrage für " + product.getName() + " von " + userWhoWantsToRent.getName();
        String text = "Hallo " + owner.getName() + ",\n\n" + "der Nutzer " + userWhoWantsToRent.getName() + " aus " + userWhoWantsToRent.getCity() + " möchte dein " + product.getName() + " vom " + startDate + " bis " + endDate + " ausleihen." +
                "\n Du kannst hier dem Nutzer direkt antworten (er erhält von uns eine Benachrichtigung auf Studileih, sowie eine Benachrichtigung per Email) oder sende " + userWhoWantsToRent.getName() + " direkt eine Nachricht an " + userWhoWantsToRent.getEmail() + "." +
                "\n\n";
        return new Message(subject, text, LocalDateTime.now().toString(), userWhoWantsToRent, owner, previousChat);
    }

    // get previous Chat between the two Users, or create new Chat
    private Chat getPreviousChat(User user1, User user2) {
        // check all chats, if there is one that has both users. If there is already one (= if those two users already sent one message to each other) we dont want to create a new chat but continue with that previous chat.
        for (Chat chat: chatService.loadAllChat()) {
            if ((chat.getUser1() == user1 || chat.getUser2() == user1) && (chat.getUser1() == user2 || chat.getUser2() == user2)) {
                return chat;
            }
        }
        // if there was none, create a new chat with those two users.
        return new Chat(user1, user2);
    }

    public List<Message> listAllMessages() {
        List<Message> messages = new ArrayList<>();
        messageRepository.findAll().forEach(messages::add);  // messages::add ist gleich wie: messages.add(message)
        return messages;
    }



}