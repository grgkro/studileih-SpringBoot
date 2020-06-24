package com.example.studileih.Service;

import com.example.studileih.Entity.Product;
import com.example.studileih.Entity.User;
import com.example.studileih.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public void saveOrUpdateUser(User user) {
        userRepository.save(user);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public List<User> listAllUser() {
        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add); // users::add ist gleich wie: users.add(user)
        return users;
    }

    public boolean addUser(User user) {
        userRepository.save(user);
        return true;
    }

    public User updateUser(User newUser, Long id) {
        User oldUser = null;

        if(getUserById(id).isPresent()){
            assert false; //assert oldUser != null;
            oldUser.setName(newUser.getName());
            oldUser.setName(newUser.getName());

            oldUser.setEmail(newUser.getEmail());
            oldUser.setPassword(newUser.getPassword());
            oldUser.setRoom(newUser.getRoom());
            oldUser.setProfilePic(newUser.getProfilePic());
            oldUser.setDorm(newUser.getDorm());
            oldUser.setCity(newUser.getCity());

            userRepository.save(oldUser);
            System.out.println(oldUser);
            return oldUser;
        }

        return oldUser;
    }

}