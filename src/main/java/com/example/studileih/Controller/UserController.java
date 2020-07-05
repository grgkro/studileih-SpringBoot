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
    private ProductService productService;

    /**
     * Die Funktion wird direkt nach Start aufgerufen und speichert 1 Beispielwohnheim/Adresse/2 Pro in die DB -> Kann später auskommentiert/gelöscht werden
     */
    @PostConstruct
    public void createBaseDataset() {
        // at the start we create some dorms into the database, but only if there are no entries yet!
        Product product1 = new Product("Haralds VW Golf", "VW 3er Golf, BJ. 1998, 100.000km", 30);
        Product product2 = new Product("Haralds Bohrmaschine", "Bosch Bohrmaschine", 0);
        List<Product> haraldsList = new ArrayList<>();
        haraldsList.add(product1);
        haraldsList.add(product2);
        User user = new User("Harald", "harald@gmx.com", "2345", haraldsList);
        userService.saveOrUpdateUser(user);
        product1.setUser(user);
        product2.setUser(user);
        productService.saveOrUpdateProduct(product1);
        productService.saveOrUpdateProduct(product2);

    }

    /**
     * @return: all users from the repository
     */
    @GetMapping("users")
    public List<UserDto> getAllUsers() {
        return userService.listAllUser();
    }

    @GetMapping("/users/{id}")
    public Optional<User> getUser(@PathVariable String id) {
        return userService.getUserById(Long.parseLong(id));
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