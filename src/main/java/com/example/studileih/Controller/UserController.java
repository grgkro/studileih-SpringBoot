package com.example.studileih.Controller;

import com.example.studileih.Dto.CityEnum;
import com.example.studileih.Dto.MessageDto;
import com.example.studileih.Dto.UserDto;
import com.example.studileih.Entity.*;
import com.example.studileih.Entity.Product;
import com.example.studileih.Service.*;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiResponse;
import org.springframework.web.multipart.MultipartFile;


import javax.annotation.PostConstruct;
import java.util.*;

@RestController
@CrossOrigin
@Api(tags = "Users API - controller methods for managing Users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private DormService dormService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ChatService chatService;

    /**
     * Die Funktion wird direkt nach Start aufgerufen und speichert 1 Beispielwohnheim/Adresse/2 Pro in die DB -> Kann später auskommentiert/gelöscht werden
     */
    @PostConstruct
    public void createBaseDataset() {

        Product product1 = new ProductBuilder().withTitle("VW 3er Golf, BJ. 1998, 100.000km").withDescription("Mein VW Golf zum Ausleihen, wiedersehen macht Freude höhö").withPrice(30).withAvailable(false).withDorm("Alexanderstraße").withCity(CityEnum.Stuttgart.toString()).build();
        Product product2 = new ProductBuilder().withTitle("Bosch Bohrmaschine").withDescription("Haralds Bohrmaschine").withPrice(0).withIsBeerOk(true).withCategory("Werkzeug").withAvailable(true).withDorm("Alexanderstraße").withCity(CityEnum.Stuttgart.toString()).build();
        Product product3 = new ProductBuilder().withTitle("Hartmuts Bohrmaschine").withDescription("Hartmuts Bohrmaschine").withPrice(5).withIsBeerOk(true).withCategory("Werkzeug").withAvailable(true).withDorm("Anna-Herrigel-Haus").withCity(CityEnum.Stuttgart.toString()).build();
        List<Product> haraldsList = new ArrayList<>();
        List<Product> hartmutsList = new ArrayList<>();
        haraldsList.add(product1);
        haraldsList.add(product2);
        hartmutsList.add(product3);
        User harald = new User("Harald", "grg.kro@gmail.com", "2345", haraldsList, dormService.getDormById(1L).get());
        User hartmut = new User("Hartmut", "georgkromer@pm.me", "5432", hartmutsList, dormService.getDormById(2L).get());
        product1.setUser(harald);
        product2.setUser(harald);
        product3.setUser(hartmut);

        // durch das Speichern der user werden die verknüpften Produkte auch gespeichert. Es ist also unnötig die Produkte mit productService.saveProduct() nochmal zu speichern.
        userService.saveOrUpdateUser(harald);
        userService.saveOrUpdateUser(hartmut);

//        Chat chatHaraldHartmut = new Chat(harald, hartmut);
//        chatService.saveOrUpdateChat(chatHaraldHartmut);
//
//        Message fromHaraldToHartmut = new Message("Harald an Hartmut", "Hey Harti", "11.11.2020 11:11:11", harald, hartmut, chatHaraldHartmut);
//        Message fromHaraldToHartmut2 = new Message("Harald an Hartmut 2", "Jo Harald", "12.11.2020 11:11:11", harald, hartmut, chatHaraldHartmut);
//        Message fromHartmutToHarald = new Message("HARTMUT an HARALD", "Ruhe Harald!", "12.11.2020 11:11:11", hartmut, harald, chatHaraldHartmut);
//        List<Message> messagesHarald = new ArrayList<>();
//        List<Message> messagesHartmut = new ArrayList<>();
//        messagesHarald.add(fromHaraldToHartmut);
//        messagesHarald.add(fromHaraldToHartmut2);
//        messagesHarald.add(fromHartmutToHarald);
//        messagesHartmut.add(fromHaraldToHartmut);
//        messagesHartmut.add(fromHaraldToHartmut2);
//        messagesHartmut.add(fromHartmutToHarald);
//        harald.setSentMessages(messagesHarald);
//        hartmut.setReceivedMessages(messagesHartmut);
//
//        messageService.saveOrUpdateMessage(fromHaraldToHartmut);
//        messageService.saveOrUpdateMessage(fromHaraldToHartmut2);
//        messageService.saveOrUpdateMessage(fromHartmutToHarald);

    }


    /**
     * @return: all users from the repository
     */
    @GetMapping("users")
    @ApiOperation(value = "Return all available users as Entities")
    public List<UserDto> getAllUsers() {
        return userService.listAllUser();
    }

    @GetMapping("/usersdto/{id}")
    @ApiOperation(value = "Return one User identified by ID as DTO")
    public UserDto getUserDto(@PathVariable Long id) {
        return userService.getUserDtoById(id);
    }
    
    @GetMapping("/usersdtoprotected/{id}")
    @ApiOperation(value = "Return one User identified by ID as DTO, protected by JWT")
    public UserDto getUserDtoProtected(@PathVariable Long id) {
        return userService.getUserDtoById(id);
    }

    /**
     * Get's an user as JSON from the angular-client and saves it as a new user into the repository
     */
    @PostMapping("saveUser")
    @ApiOperation(value = "Add/update new User as DTO")
    public Boolean saveUser(@RequestBody UserDto userDto) {
        userService.saveOrUpdateUser(new User(userDto.getName()));
        return true;
    }

    @PostMapping(path = "/users")
    @ApiOperation(value = "Add new User as Entity")
    public ResponseEntity<String> addUser(String name, String email, String password, Long dormId, MultipartFile profilePic) {

        Dorm dorm = dormService.getDormById(dormId).get();
        User newUser = new User(name, email, password, dorm);
        userService.addUser(newUser);
        userService.saveUserPic(profilePic, newUser.getId() );
        return ResponseEntity.status(HttpStatus.OK).body("User erfolgreich angelegt.");
    }

    @DeleteMapping(value = "/users/{id}")
    @ApiOperation(value = "Remove User identified by ID")
    public void deleteUser(@PathVariable Long id) {

        userService.deleteUser(id);
        // everytime a user gets deleted, we should also check, if there are still messages connected to him. but only the message if both users, the sender and the receiver got deleted.
        List<MessageDto> allMessages = messageService.loadAll();
        allMessages.stream().forEach(messageDto -> {
            if (messageDto.getSender() == null && messageDto.getReceiver() == null) {
                messageService.deleteMessage(messageDto.getId());
            }
        });
    }

    @PutMapping(value = "/users/{id}")
    @ApiOperation(value = "Update User identified by ID")
    public void updateUser(@RequestBody User user, @PathVariable Long id) {
        userService.updateUser(user, id);
    }

}