package com.example.studileih.Controller;

import com.example.studileih.Dto.MessageDto;
import com.example.studileih.Dto.UserDto;
import com.example.studileih.Entity.User;
import com.example.studileih.Service.MessageService;
import com.example.studileih.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.multipart.MultipartFile;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@Api(tags = "Users API - controller methods for managing Users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;



    /**
     * @return: all users of one dorm from the repository
     */
    @GetMapping("users/usersByDorm")
    @ApiOperation(value = "Return all available users from one dorm as Entities")
    public List<UserDto> getAllUsersOfOneDorm(Principal user) {
        List<UserDto> allUsers = userService.listAllUser();
        return allUsers.stream().filter(userDto -> userDto.getDormId() == userService.getActiveUserByName(user.getName()).getDorm().getId()).collect(Collectors.toList());
    }

    /**
     * @return: the user who ownes the product
     */
    @GetMapping("users/owner/{productId}")
    @ApiOperation(value = "Return the user who ownes the product")
    public UserDto getOwner(@PathVariable Long productId) {
        return userService.getOwner(productId);
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
    @ApiOperation(value = "Resgisters/Adds a new User with encoded Password in DB")
    public ResponseEntity<String> registerUser(String name, String email, String password, String city, Long dormId, MultipartFile profilePic) {
        return userService.registerUser(name, email, password, city, dormId, profilePic);
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