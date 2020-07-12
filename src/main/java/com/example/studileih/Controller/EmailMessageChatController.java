package com.example.studileih.Controller;

import com.example.studileih.Dto.MessageDto;
import com.example.studileih.Dto.UserDto;
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
    private ModelMapper modelMapper;  //modelMapper konvertiert Entities in DTOs (modelMapper Dependency muss in pom.xml drin sein)


    /**
     * Die Funktion wird direkt nach Start aufgerufen und speichert 1 Beispielwohnheim/Adresse/2 Pro in die DB -> Kann später auskommentiert/gelöscht werden
     */
    @PostConstruct
    public void createBaseDataset() {
        // sends an email from studileih@gmail.com. I think you need to be on a Windows PC that this works! else go to the application.properties and uncomment your system password (Linux, Mac)... (https://www.baeldung.com/spring-email)
        emailService.sendSimpleMessage("georgkromer@pm.me", "server started", "yolo" );
    }

    /*
     * sends an "Ausleihanfrage" Email with the startdate, enddate, product, ausleihender user etc. to the product owner`s email address
     */
    @PostMapping("/emails/sendEmail")
    @ApiOperation(value = "Sends an \"Ausleihanfrage\" Email from studileih@gmail.com to the owner of the product")
    public ResponseEntity<String> sendEmailToOwner (@RequestParam("startDate") String startDate, String endDate, String productId, String userId, String ownerId){
        try {
            // get the two users and the product
            User userWhoWantsToRent = userService.getUserById(Long.parseLong(userId)).get();   // the id always comes as a string from angular, even when you send it as a number in angular... getUserById returns an Optional<User> -> we immediately take the User from the Optional with with .get(). Maybe bad idea?
            User owner = userService.getUserById(Long.parseLong(ownerId)).get();
            Product product = productService.getProductById(Long.parseLong(productId)).get();
            // sends an email from studileih@gmail.com. I think you need to be on a Windows PC that this works! else go to the application.properties and uncomment your system password (Linux, Mac)... (https://www.baeldung.com/spring-email)
            return emailService.sendEmailToOwner(startDate, endDate, product, userWhoWantsToRent, owner);
        } catch (NoSuchElementException e) {
            System.out.println(e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Interner Datenbankfehler - Produkt, Produktbesitzer oder Anfragender User konnte nicht geladen werden.");
        }
    }

    /*
     * sends an "Ausleihanfrage" Message with the startdate, enddate, product, ausleihender user etc. to the product owner in Studileih (like a intern Facebook message from one user to another)
     */
    @PostMapping("/messages/sendMessage")
    @ApiOperation(value = "Saves a message and connects it with the sending and the receiving user in the Database")
    public ResponseEntity<String> saveMessage (@RequestParam("startDate") String startDate, String endDate, String productId, String userId, String ownerId){
        try {
            // get the two users and the product
            User userWhoWantsToRent = userService.getUserById(Long.parseLong(userId)).get();   // the id always comes as a string from angular, even when you send it as a number in angular... getUserById returns an Optional<User> -> we immediately take the User from the Optional with with .get(). Maybe bad idea?
            User owner = userService.getUserById(Long.parseLong(ownerId)).get();
            Product product = productService.getProductById(Long.parseLong(productId)).get();
            // transfer the message to the messageService where it will be saved to the database
            return messageService.sendMessageToOwner(startDate, endDate, product, userWhoWantsToRent, owner);
        } catch (NoSuchElementException e) {
            System.out.println(e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Interner Datenbankfehler - Produkt, Produktbesitzer oder Anfragender User konnte nicht geladen werden.");
        }

    }

    /*
    * loads all messages as MessageDtos
     */
    @GetMapping("/messages/messages")
    @ApiOperation(value = "Return all available messages converted to DTOs")
    public List<MessageDto> loadMessages (){
        List<Message> allMessages = messageService.listAllMessages();
        System.out.println(allMessages.get(0).toString());
        List<MessageDto> allMessagesDtos = allMessages.stream()                 // List<Message> muss zu List<MessageDto> konvertiert werden. Hier tun wir zuerst die List<Message> in einen Stream umwandeln
                .map(this::convertMessageToDto)            // Dann jedes Message ausm Stream herausnehmen und zu MessageDto umwandeln
                .collect(Collectors.toList());      // und dann den neuen Stream als List<MessageDto> einsammeln.
        return allMessagesDtos;
    }

    /**
     * Converts a Message to a MessageDto. The createdAt and updatedAt Dates are converted to simple Strings, because Date is Java specific and can't be send to Angular.
     * @param message
     * @return messageDto
     */
    private MessageDto convertMessageToDto(Message message) {
        MessageDto messageDto = modelMapper.map(message, MessageDto.class);
        // falls ein Product ein CreatedAt oder UpdatedAt Value hat, muss der zu einem String konvertiert werden. Falls nicht darf das Date nicht konvertiert werden, sonst gibts NullPointerExceptions.
        return messageDto;
    }

}