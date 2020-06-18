package com.example.studileih.Controller;

import com.example.studileih.Dto.UserDto;
import com.example.studileih.Entity.*;
import com.example.studileih.Entity.Product;
import com.example.studileih.Service.ProductService;
import com.example.studileih.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.*;

@RestController
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService ProductService;

    /**
     * Die Funktion wird direkt nach Start aufgerufen und speichert 1 Beispielwohnheim/Adresse/2 Pro in die DB -> Kann später auskommentiert/gelöscht werden
     */
    @PostConstruct
    public void createBaseDataset() {
        // can be deleted later
        Dorm inDerAu = new Dorm("In der Au 16");  // Für später: inDerAu.setCreatedAt(Calendar.getInstance().getTime());
        Address inderAuAddress = new Address("In der Au 16", inDerAu);
        Product product1 = new Product("Haralds VW Golf");
        List<Product> haraldsList = new ArrayList<>();
        haraldsList.add(product1);
        User user = new User("Harald", "harald@gmx.com", "2345", haraldsList, inDerAu);
        userService.saveOrUpdateUser(user);

        product1.setUser(user);
        ProductService.saveOrUpdateProduct(product1);
    }

    /**
     * @return: all users from the repository
     */
    @GetMapping("getAllUsers")
    public List<User> getAllUsers() {
        return userService.listAllUser();
    }

    /**
     * Get's an user as JSON from the angular-client and saves it as a new user into the repository
     */
    @PostMapping("saveUser")
    public Boolean saveUser(@RequestBody UserDto userDto) {
        userService.saveOrUpdateUser(new User(userDto.getName()));
        return true;
    }

}