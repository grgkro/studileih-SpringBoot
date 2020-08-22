package com.example.studileih.Controller;

import com.example.studileih.Dto.ChatDto;
import com.example.studileih.Dto.MessageDto;
import com.example.studileih.Dto.UserDto;
import com.example.studileih.Entity.Chat;
import com.example.studileih.Entity.Message;
import com.example.studileih.Entity.Product;
import com.example.studileih.Entity.User;
import com.example.studileih.Service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.security.Principal;
import java.text.ParseException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@Api(tags = "Emails and Messages API - controller methods for managing Emails & Messages")
public class EmailMessageChatController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private ModelMapper modelMapper;  //modelMapper konvertiert Entities in DTOs (modelMapper Dependency muss in pom.xml drin sein)


    /**
     * Die Funktion wird direkt nach Start aufgerufen und speichert 1 Beispielwohnheim/Adresse/2 Pro in die DB -> Kann später auskommentiert/gelöscht werden
     */
    @PostConstruct
    public void createBaseDataset() {
        // sends an email from studileih@gmail.com. I think you need to be on a Windows PC that this works! else go to the application.properties and uncomment your system password (Linux, Mac)... (https://www.baeldung.com/spring-email)
        emailService.sendSimpleMessage("georgkromer@pm.me", "server started", "yolo");
    }

    /*
     * sends an "Ausleihanfrage" Email with the startdate, enddate, product, ausleihender user etc. to the product owner`s email address
     */
    @PostMapping("/emails/sendEmail")
    @ApiOperation(value = "Sends an \"Ausleihanfrage\" Email from studileih@gmail.com to the owner of the product")
    public ResponseEntity<String> sendEmailToOwner(String startDate, String endDate, String pickUpTime, String returnTime, Long productId, Principal user, Long ownerId) {
        try {
            // we don't send the info, which user is sending the request from the frontend anymore. Instead we always take the logged in user (principal) from spring security -> thus this can't be manipulated anymore.
            return emailService.sendEmailToOwner(startDate, endDate, pickUpTime, returnTime, productService.getProductEntityById(productId), userService.getActiveUserByName(user.getName()), userService.getUserById(ownerId).get());
        } catch (NoSuchElementException e) {
            System.out.println(e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Interner Datenbankfehler - Produkt, Produktbesitzer oder Anfragender User konnte nicht geladen werden.");
        }
    }

    /*
     * sends an "Ausleihanfrage" Message with the startdate, enddate, product, ausleihender user etc. to the product owner in Studileih (like a intern Facebook message from one user to another)
     */
    @PostMapping("/messages/sendMessage")
    @ApiOperation(value = "sends an \"Ausleihanfrage\" Message with the startdate, enddate, product, ausleihender user etc. to the product owner in Studileih (like a intern Facebook message from one user to another)")
    public ResponseEntity<String> sendMessageToOwner(String startDate, String endDate, String pickUpTime, String returnTime, Long productId, Principal user, Long ownerId) {
        try {
            // transfer the message to the messageService where it will be saved to the database and send as email
            return messageService.sendMessageToOwner(startDate, endDate, pickUpTime, returnTime, productService.getProductEntityById(productId), userService.getActiveUserByName(user.getName()), userService.getUserById(ownerId).get());
        } catch (NoSuchElementException e) {
            System.out.println(e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Interner Datenbankfehler - Produkt, Produktbesitzer oder Anfragender User konnte nicht geladen werden.");
        }
    }

    /*
     * updates a Message with the receivedAt
     */
    @PostMapping("/messages/updateMessage")
    @ApiOperation(value = "Updates a Message with the receivedAt timestamp (gets called when the receiver opened the message in the frontend)")
    public ResponseEntity<String> updateMessageAsRead(Long chatId, Long messageId, String receivedAt) {
       return messageService.updateMessageAsReceived(chatId, messageId, receivedAt);
    }

    /*
     * updates a Message with the receivedAt
     */
    @PostMapping("/messages/sendReply")
    @ApiOperation(value = "Saves a reply message to a 'Ausleihanfrage' into the database at the specified chat, so that the other chat partner can see it next time on Studileih.de")
    public ResponseEntity<String> sendReply(String subject, String messageText, String sendetAt, Long chatId, Principal user) {
        return messageService.sendReply(subject, messageText, sendetAt, chatService.getChatById(chatId).get(), userService.getUserById(userService.getActiveUserByName(user.getName()).getId()).get());
    }

    /*
     * updates a Message with the receivedAt
     */
    @PostMapping("/messages/sendEmailReply")
    @ApiOperation(value = "Sends the reply message with all previous messages as Email to the chat partner")
    public ResponseEntity<String> sendEmailReply(String subject, String messageText, String sendetAt, Long chatId, Principal user) {
      return emailService.prepareEmailReply(subject, messageText, sendetAt, chatService.getChatById(chatId).get(), userService.getUserById(userService.getActiveUserByName(user.getName()).getId()).get());
    }

//    /*
//     * loads all messages as MessageDtos
//     */
//    @GetMapping("/messages/messages")
//    @ApiOperation(value = "Return all available messages converted to DTOs")
//    public List<MessageDto> loadMessages() {
//        return messageService.loadAll();
//    }

    /*
     * loads all chats as ChatDtos
     */
    @GetMapping("/chats/")
    @ApiOperation(value = "Return all available chats of one User converted to DTOs")
    public List<ChatDto> loadChatsByUser(Principal user) {
        // We don't send the userId as PathVariable anymore -> could get manipulated. Instead we always only get the logged in user from spring security and then get only his chats
        return chatService.findChatsByUserId(userService.getActiveUserByName(user.getName()).getId());
    }



}