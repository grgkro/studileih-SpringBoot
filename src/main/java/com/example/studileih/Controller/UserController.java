package com.example.studileih.Controller;

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

    /**
     * Die Funktion wird direkt nach Start aufgerufen und speichert 1 Beispielwohnheim/Adresse/2 Pro in die DB -> Kann später auskommentiert/gelöscht werden
     */
    @PostConstruct
    public void createBaseDataset() {
        // at the start we create some dorms into the database, but only if there are no entries yet!
        Product product1 = new Product("Haralds VW Golf", "VW 3er Golf, BJ. 1998, 100.000km", 30);
        Product product2 = new Product("Haralds Bohrmaschine", "Bosch Bohrmaschine", 0);
        Product product3 = new Product("Hartmuts Bohrmaschine", "Bosch Bohrmaschine", 5);
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

        userService.saveOrUpdateUser(harald);
        userService.saveOrUpdateUser(hartmut);

        Message fromHaraldToHartmut = new Message("TestEinsZwoEinsZwo", "Testtesttestte", "11.11.2020 11:11:11", harald, hartmut);
        Message fromHaraldToHartmut2 = new Message("TestEinsZwoEinsZwo2", "Testtesttestte2", "12.11.2020 11:11:11", harald, hartmut);
        Message fromHartmutToHarald = new Message("TestEinsZwoEinsZwo2", "Testtesttestte2", "12.11.2020 11:11:11", hartmut, harald);
        List<Message> messagesHarald = new ArrayList<>();
        List<Message> messagesHartmut = new ArrayList<>();
        messagesHarald.add(fromHaraldToHartmut);
        messagesHarald.add(fromHaraldToHartmut);
        messagesHarald.add(fromHartmutToHarald);
        messagesHartmut.add(fromHaraldToHartmut);
        messagesHartmut.add(fromHaraldToHartmut2);
        messagesHartmut.add(fromHartmutToHarald);
        harald.setSentMessages(messagesHarald);
        hartmut.setReceivedMessages(messagesHartmut);



        messageService.saveOrUpdateMessage(fromHaraldToHartmut);

        userService.saveOrUpdateUser(harald);
        userService.saveOrUpdateUser(hartmut);




        // durch das Speichern der user werden die verknüpften Produkte auch gespeichert. Es ist also unnötig die Produkte mit productService.saveProduct() nochmal zu speichern.
        // sends an email from studileih@gmail.com. I think you need to be on a Windows PC that this works! else go to the application.properties and uncomment your system password (Linux, Mac)... (https://www.baeldung.com/spring-email)
        emailService.sendSimpleMessage("georgkromer@pm.me", "server started", "yolo" );
    }


    /**
     * @return: all users from the repository
     */
    @GetMapping("users")
    @ApiOperation(value = "Return all available users as Entities")
    public List<UserDto> getAllUsers() {
        return userService.listAllUser();
    }

    @GetMapping("/users/{id}")
    @ApiOperation(value = "Return one User identified by ID")
    public Optional<User> getUser(@PathVariable String id) {
        return userService.getUserById(Long.parseLong(id));
    }
    
    @GetMapping("/usersdto/{id}")
    @ApiOperation(value = "Return one User identified by ID as DTO")
    public List<UserDto> getUserDto(@PathVariable Long id) {
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

    @PostMapping(path = "/users", consumes = "application/json", produces = "application/json")
    @ApiOperation(value = "Add new User as Entity")
    public boolean addUser(@RequestBody User user) {
        return userService.addUser(user);
    }

    @DeleteMapping(value = "/users/{id}")
    @ApiOperation(value = "Remove User identified by ID")
    public void deleteUser(@PathVariable Long id) {

        userService.deleteUser(id);
        List<MessageDto> allMessages = messageService.loadAll();
        allMessages.stream().forEach(messageDto -> {
            if (messageDto.getSender() == null && messageDto.getReceiver() == null) {
                messageService.deleteMessage(messageDto.getId());
            }});
    }

    @PutMapping(value = "/users/{id}")
    @ApiOperation(value = "Update User identified by ID")
    public void updateUser(@RequestBody User user, @PathVariable Long id) {
        userService.updateUser(user, id);
    }

    /*
     * sends an "Ausleihanfrage" Email with the startdate, enddate, product, ausleihender user etc. to the product owner`s email address
     */
    @PostMapping("/emails/sendEmail")
    public ResponseEntity<String> sendEmailToOwner (@RequestParam("startDate") String startDate, String endDate, String productId, String userId, String ownerId){
        // get the two users and the product
        User userWhoWantsToRent = userService.getUserById(Long.parseLong(userId)).get();   // the id always comes as a string from angular, even when you send it as a number in angular... getUserById returns an Optional<User> -> we immediately take the User from the Optional with with .get(). Maybe bad idea?
        User owner = userService.getUserById(Long.parseLong(ownerId)).get();
        Product product = productService.getProductById(Long.parseLong(productId)).get();
        // send the email
        if (product != null && owner != null && userWhoWantsToRent != null) {
            if (owner.getEmail() != null) {
                // sends an email from studileih@gmail.com. I think you need to be on a Windows PC that this works! else go to the application.properties and uncomment your system password (Linux, Mac)... (https://www.baeldung.com/spring-email)
                return emailService.sendEmailToOwner(startDate, endDate, product, userWhoWantsToRent, owner);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Der Besitzer dieses Produkts hat keine Email Addresse hinterlegt.");  // sobald Email Addresse Pflichtfeld ist kann das weg.
            }

        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Interner Datenbankfehler - Produkt, Produktbesitzer oder Anfragender User konnte nicht geladen werden.");
        }
    }

}