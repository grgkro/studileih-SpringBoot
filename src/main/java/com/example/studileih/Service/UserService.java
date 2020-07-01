package com.example.studileih.Service;

import com.example.studileih.Dto.ProductDto;
import com.example.studileih.Dto.UserDto;
import com.example.studileih.Entity.Product;
import com.example.studileih.Entity.User;
import com.example.studileih.Repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;  //modelMapper konvertiert Entities in DTOs (modelMapper Dependency muss in pom.xml drin sein)

    public void saveOrUpdateUser(User user) {
        userRepository.save(user);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public List<UserDto> listAllUser() {
        // get all users from DB
        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add); // users::add ist gleich wie: users.add(user)
        // map all users to userDtos
        List<UserDto> userDtos = new ArrayList<>();
        return users.stream()                 // List<Product> muss zu List<ProductDto> konvertiert werden. Hier tun wir zuerst die List<Product> in einen Stream umwandeln
                .map(this::convertUserToDto)            // Dann jedes Product ausm Stream herausnehmen und zu ProductDto umwandeln
                .collect(Collectors.toList());      // und dann den neuen Stream als List<ProductDto> einsammeln.
    }

    /**
     * Converts a User to a UserDto. The createdAt and updatedAt Dates are converted to simple Strings, because Date is Java specific and can't be send to Angular.
     * @param user
     * @return userDto
     */
    private UserDto convertUserToDto(User user) {
        UserDto userDto = modelMapper.map(user, UserDto.class);
        // falls ein User ein CreatedAt oder UpdatedAt Value hat, muss der zu einem String konvertiert werden. Falls nicht darf das Date nicht konvertiert werden, sonst gibts NullPointerExceptions.
        if (user.getCreatedAt() != null) userDto.setCreatedAt(user.getCreatedAt(), ZonedDateTime.now(ZoneId.systemDefault()).toString()); //konvertiert Date zu String -> einfachhaltshaber nimmts immer die Zeitzone des Servers (ZoneId.systemdefault), vielleicht irgendwann mal durch die Zeitzone des Nutzers ersetzen.
        if (user.getUpdatedAt() != null) userDto.setUpdatedAt(user.getUpdatedAt(), ZonedDateTime.now(ZoneId.systemDefault()).toString());
        return userDto;
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