package com.example.studileih.Controller;

import com.example.studileih.Dto.UserDto;
import com.example.studileih.Entity.*;
import com.example.studileih.Entity.Product;
import com.example.studileih.Service.ProductService;
import com.example.studileih.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
        Product product1 = new Product("Haralds VW Golf");
        List<Product> haraldsList = new ArrayList<>();
        haraldsList.add(product1);
        User user = new User("Harald", "harald@gmx.com", "2345", haraldsList, "In der Au");
        userService.saveOrUpdateUser(user);

        product1.setUser(user);
        ProductService.saveOrUpdateProduct(product1);
    }

    /**
     * @return: all users from the repository
     */
    @GetMapping("users")
    public List<User> getAllUsers() {
        return userService.listAllUser();
    }

    @GetMapping("/users/{id}")
    public Optional<User> getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    /**
     * Get's an user as JSON from the angular-client and saves it as a new user into the repository
     */
    @PostMapping("saveUser")
    public Boolean saveUser(@RequestBody UserDto userDto) {
        userService.saveOrUpdateUser(new User(userDto.getName()));
        return true;
    }

    @PostMapping(path = "/users", consumes = "application/json", produces = "application/json")
    public boolean addUser(@RequestBody User user) {
        return userService.addUser(user);
    }

    @DeleteMapping(value = "/users/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @PutMapping(value = "/users/{id}")
    public void updateUser(@RequestBody User user, @PathVariable Long id) {
        userService.updateUser(user, id);
    }

}