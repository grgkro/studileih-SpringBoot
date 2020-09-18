package com.example.studileih.Controller;

import com.example.studileih.Dto.*;
import com.example.studileih.Service.ChatService;
import com.example.studileih.Service.EmailService;
import com.example.studileih.Service.MessageService;
import com.example.studileih.Service.ProductService;
import com.example.studileih.Service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

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

    /*
     * sends an "Ausleihanfrage" Email with the startdate, enddate, product, ausleihender user etc. to the product owner`s email address
     */
    @PostMapping("/emails/sendEmail")
    @ApiOperation(value = "Sends an \"Ausleihanfrage\" Email from studileih@gmail.com to the owner of the product")
    public ResponseEntity<String> sendEmailToOwner(String startDate, String endDate, String pickUpTime, String returnTime, Long productId, Principal user) {
        try {
            // we don't send the info, which user is sending the request from the frontend anymore. Instead we always take the logged in user (principal) from spring security -> thus this can't be manipulated anymore.
            return emailService.sendEmailToOwner(startDate, endDate, pickUpTime, returnTime, productService.getProductEntityById(productId), userService.getActiveUserByName(user.getName()), productService.getProductEntityById(productId).getUser());
        } catch (NoSuchElementException e) {
            System.out.println(e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Interner Datenbankfehler - Produkt, Produktbesitzer oder Anfragender User konnte nicht geladen werden.");
        }
    }

    /*
     * sends an "Wohnheim hinzufügen" Email to the admin
     */
    @PostMapping("/emails/sendEmail/admin")
    @ApiOperation(value = "Sends an \"Wohnheim hinzufügen\" Email to the admin")
    public ResponseEntity<String> sendEmailToAdmin(@RequestBody NewDormRequest newDorm, Principal userDetails) {
        try {
            return emailService.sendEmailToAdmin(newDorm.getName(), newDorm.getCity(), newDorm.getStreet(), newDorm.getHouseNumber(), userDetails);
        } catch (NoSuchElementException e) {
            System.out.println(e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Interner Datenbankfehler - Wohnheim Anfrage konnte nicht verarbeitet werden.");
        }
    }


    /*
     * sends an "Ausleihanfrage" Message with the startdate, enddate, product, ausleihender user etc. to the product owner in Studileih (like a intern Facebook message from one user to another)
     */
    @PostMapping("/messages/sendMessage")
    @ApiOperation(value = "sends an \"Ausleihanfrage\" Message with the startdate, enddate, product, ausleihender user etc. to the product owner in Studileih (like a intern Facebook message from one user to another)")
    public ResponseEntity<String> sendMessageToOwner(String startDate, String endDate, String pickUpTime, String returnTime, Long productId, Principal user) {
        try {
            // transfer the message to the messageService where it will be saved to the database and send as email
            return messageService.sendMessageToOwner(startDate, endDate, pickUpTime, returnTime, productService.getProductEntityById(productId), userService.getActiveUserByName(user.getName()), productService.getProductEntityById(productId).getUser());
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
    public ResponseEntity<String> sendReply(@RequestBody MessageDtoForReceiving message, Principal user) {
        return messageService.sendReply(message.getSubject(), message.getText(), message.getSendetAt(), chatService.getChatById(message.getChatId()).get(), userService.getUserById(userService.getActiveUserByName(user.getName()).getId()).get());
    }

    /*
     * updates a Message with the receivedAt
     */
    @PostMapping("/messages/sendEmailReply")
    @ApiOperation(value = "Sends the reply message with all previous messages as Email to the chat partner")
    public ResponseEntity<String> sendEmailReply(@RequestBody MessageDtoForReceiving message, Principal user) {
      return emailService.prepareEmailReply(message.getSubject(), message.getText(), message.getSendetAt(), chatService.getChatById(message.getChatId()).get(), userService.getUserById(userService.getActiveUserByName(user.getName()).getId()).get());
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
     * loads all chats as ChatDtos TODO: Security Risk -> sends too much info
     */
    @GetMapping("/chats/")
    @ApiOperation(value = "Return all available chats of one User converted to DTOs")
    public List<ChatDtoForResponding> loadChatsByUser(Principal user) {
        List<ChatDto> chats = chatService.findChatsByUserId(userService.getActiveUserByName(user.getName()).getId());
        //we don't send the full chat with all detail info, but only a much smaller version
        List<ChatDtoForResponding> chatDtosForResponding = new ArrayList<>();
        for (ChatDto chat: chats) {
            List<MessageDtoForResponding> dtos = chatService.getAllMessagesByChatId(chat.getId());
            ChatDtoForResponding chatDto = new ChatDtoForResponding(chat.getId(), chat.getUser1().getName(), chat.getUser2().getName(), dtos);
            chatDtosForResponding.add(chatDto);
        }
        System.out.println("chats" + chatDtosForResponding);
        return chatDtosForResponding;
    }

    /*
     * loads one chat as ChatDtoForResponding
     */
    @GetMapping("/chats/{id}")
    @ApiOperation(value = "Return one chat by Id converted to secure ChatDtoForResponding")
    public ChatDtoForResponding getChatById(@PathVariable Long id) {
        return chatService.getChatDtoForRespondingById(id);
    }

    /*
     * loads all messages of one chat
     */
    @GetMapping("/chats/messagesByChatId/{id}")
    @ApiOperation(value = "Return all available messages of one Chat converted to secure MessageDtosForResponding")
    public ResponseEntity loadMessagesByChatId(@PathVariable Long id) {
// We don't send the userId as PathVariable anymore -> could get manipulated. Instead we always only get the logged in user from spring security and then get only his chats
        List<MessageDtoForResponding> dtos = chatService.getAllMessagesByChatId(id);
        if (dtos == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chat mit Id " + id + " konnte nicht gefunden werden.");
        }
        return ResponseEntity.status(HttpStatus.OK).body(dtos);
    }

@DeleteMapping("/delete/chat/{id}")
@ApiOperation(value = "Delete Chat by id")
public ResponseEntity deleteChat(@PathVariable Long id) {
    return chatService.deleteChat(id);
}


}