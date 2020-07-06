package com.example.studileih.Controller;

import com.example.studileih.Dto.UserDto;
import com.example.studileih.Entity.*;
import com.example.studileih.Entity.Product;
import com.example.studileih.Service.ProductService;
import com.example.studileih.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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
    private ProductService ProductService;

    /**
     * Die Funktion wird direkt nach Start aufgerufen und speichert 1 Beispielwohnheim/Adresse/2 Pro in die DB -> Kann später auskommentiert/gelöscht werden
     */
	/*
	 * @PostConstruct public void createBaseDataset() { // can be deleted later
	 * Product product1 = new Product("Haralds VW Golf"); List<Product> haraldsList
	 * = new ArrayList<>(); haraldsList.add(product1); User user = new
	 * User("Harald", "harald@gmx.com", "2345", haraldsList, "In der Au");
	 * userService.saveOrUpdateUser(user);
	 * 
	 * product1.setUser(user); ProductService.saveOrUpdateProduct(product1); }
	 */

    /**
     * @return: all users from the repository
     */
    @GetMapping("users")
    @ApiOperation(value = "Return all available users as Entities")
    public List<User> getAllUsers() {
        return userService.listAllUser();
    }

    @GetMapping("/users/{id}")
    @ApiOperation(value = "Return one User identified by ID")
    public Optional<User> getUser(@PathVariable Long id) {
        return userService.getUserById(id);
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
    }

    @PutMapping(value = "/users/{id}")
    @ApiOperation(value = "Update User identified by ID")
    public void updateUser(@RequestBody User user, @PathVariable Long id) {
        userService.updateUser(user, id);
    }

}