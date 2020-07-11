package com.example.studileih.Service;

import com.example.studileih.Dto.MessageDto;
import com.example.studileih.Dto.UserDto;
import com.example.studileih.Entity.Message;
import com.example.studileih.Entity.User;
import com.example.studileih.Repository.MessageRepository;
import com.example.studileih.Repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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



}