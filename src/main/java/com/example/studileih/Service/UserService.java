package com.example.studileih.Service;

import com.example.studileih.Dto.ProductDto;
import com.example.studileih.Dto.UserDto;
import com.example.studileih.Entity.Product;
import com.example.studileih.Entity.User;
import com.example.studileih.Repository.UserRepository;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImageService imageService;

    @Autowired
    private ModelMapper modelMapper;  //modelMapper konvertiert Entities in DTOs (modelMapper Dependency muss in pom.xml drin sein)

//    public boolean usernameExists(String username) {
//        return userRepository.existsByUsernameIgnoreCase(username);
//    }



    public void saveOrUpdateUser(User user) {
        userRepository.save(user);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    //Wir wollen ein DTO zurückbekommen, damit keine unendliche Rekursion entsteht
    public UserDto getUserDtoById(Long id) {
    	User user= userRepository.findById(id).get();
    		UserDto userDto = modelMapper.map(user, UserDto.class);
        System.out.println(userDto);
    	return userDto;
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }


    public List<UserDto> listAllUser() {
        // get all users from DB
        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add); // users::add ist gleich wie: users.add(user)
        // map all users to userDtos (das ummappen klappt, obwohl es im UserDto nur eine Variable "private Long dormId" gibt und im User das als richtiges Dorm drin ist (private Dorm dorm). der modelmapper scheint da einfach gut zu raten, dass er von user nur die dorm.id nehmen muss und sie in dormId im Dto kommt.
        return users.stream()                 // List<Product> muss zu List<ProductDto> konvertiert werden. Hier tun wir zuerst die List<Product> in einen Stream umwandeln
                .map(this::convertUserToDto)            // Dann jedes Product ausm Stream herausnehmen und zu ProductDto umwandeln
                .collect(Collectors.toList());      // und dann den neuen Stream als List<ProductDto> einsammeln.
    }

    // -----------Funktioniert nicht!------------
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
//            oldUser.setDorm(newUser.getDorm());
            oldUser.setCity(newUser.getCity());

            userRepository.save(oldUser);
            System.out.println(oldUser);
            return oldUser;
        }

        return oldUser;
    }

    public ResponseEntity saveUserPic(MultipartFile file, Long userId){
        User user = getActiveUser(userId);                                      // We first load the user, for whom we wanna save the profile pic.
        if (user == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("user with Id" + userId + " doesn't exist in db.");    // Returns a status = 404 response
        if (user.getProfilePic() != null)
            deleteOldProfilePic(user);            // deletes old User Pic, so that there's always only one profile pic
        ResponseEntity response = imageService.storeImage(file, "user", user.getId());  //übergibt das Foto zum Speichern an imageService und gibt den Namen des Fotos zum gerade gespeicherten Foto zurück als Response Body. falls Speichern nicht geklappt hat kommt response mit Fehlercode zurück (400 oder ähnliches)
        if (response.getStatusCodeValue() == 200) {                         // if saving Pfoto was successfull => response status = 200...
            System.out.println("Unter folgendem Namen wurde das Foto lokal (src -> main -> resources -> images) gespeichert: " + file.getOriginalFilename());
            user.setProfilePic(file.getOriginalFilename());
            saveOrUpdateUser(user);                             //updated den User in der datenbank, damit der Fotoname da auch gespeichert ist.
        }
        return response;
    }

    private void deleteOldProfilePic (User user){
        try {
            Resource resource = imageService.loadUserProfilePic(user.getProfilePic(), user);
            new File(resource.getURI()).delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public User getActiveUser (Long userId){
        try {
            Optional<User> optionalEntity = getUserById(userId);
            return optionalEntity.get();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

}