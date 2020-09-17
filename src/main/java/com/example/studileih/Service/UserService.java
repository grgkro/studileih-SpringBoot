package com.example.studileih.Service;

import com.example.studileih.Dto.CityEnum;
import com.example.studileih.Dto.ProductDto;
import com.example.studileih.Dto.UserDto;
import com.example.studileih.Dto.UserDtoForEditing;
import com.example.studileih.Entity.Dorm;
import com.example.studileih.Entity.Product;
import com.example.studileih.Entity.User;
import com.example.studileih.Repository.UserRepository;

import com.example.studileih.Security.JwtUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImageService imageService;

    @Autowired
    private ProductService productService;

    @Autowired
    private DormService dormService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ModelMapper modelMapper;  //modelMapper konvertiert Entities in DTOs (modelMapper Dependency muss in pom.xml drin sein)




    /**
     * Die Funktion wird direkt nach Start aufgerufen und speichert 1 Beispielwohnheim/Adresse/2 Pro in die DB -> Kann später auskommentiert/gelöscht werden
     */
    @PostConstruct
    public void createBaseDataset() {
        if (productService.listAllProducts().isEmpty()) {

            Product product1 = new ProductBuilder().withTitle("VW 3er Golf, BJ. 1998, 100.000km").withDescription("Mein VW Golf zum Ausleihen, wiedersehen macht Freude höhö").withPrice(30).withAvailable(false).withDorm("Alexanderstraße").withCity(CityEnum.Stuttgart.toString()).build();
            Product product2 = new ProductBuilder().withTitle("Bosch Bohrmaschine").withDescription("Haralds Bohrmaschine").withPrice(0).withIsBeerOk(true).withCategory("Werkzeug").withAvailable(true).withDorm("Alexanderstraße").withCity(CityEnum.Stuttgart.toString()).build();
            Product product3 = new ProductBuilder().withTitle("Hartmuts Bohrmaschine").withDescription("Hartmuts Bohrmaschine").withPrice(5).withIsBeerOk(true).withCategory("Werkzeug").withAvailable(true).withDorm("Anna-Herrigel-Haus").withCity(CityEnum.Stuttgart.toString()).build();
            List<Product> haraldsList = Stream.of(product1, product2).collect(Collectors.toList());
            List<Product> hartmutsList = Stream.of(product3).collect(Collectors.toList());
            String HaraldsEncodedPassword = passwordEncoder.encode("2345");
            String HartmutsEncodedPassword = passwordEncoder.encode("5432");
            User harald = new User("Harald", "grg.kro@gmail.com", HaraldsEncodedPassword, haraldsList, dormService.getDormById(1L).get());
            User hartmut = new User("Hartmut", "georgkromer@pm.me", HartmutsEncodedPassword, hartmutsList, dormService.getDormById(2L).get());
            product1.setUser(harald);
            product2.setUser(harald);
            product3.setUser(hartmut);

            // durch das Speichern der user werden die verknüpften Produkte auch gespeichert. Es ist also unnötig die Produkte mit productService.saveProduct() nochmal zu speichern.
            saveOrUpdateUser(harald);
            saveOrUpdateUser(hartmut);
        }

    }


    public void saveOrUpdateUser(User user) {
        userRepository.save(user);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    //Wir wollen ein DTO zurückbekommen, damit keine unendliche Rekursion entsteht
    public UserDto getUserDtoById(Long id) {
        User user = userRepository.findById(id).get();
        UserDto userDto = modelMapper.map(user, UserDto.class);
        System.out.println(userDto);
        return userDto;
    }

    public UserDtoForEditing getUserDtoForEditing(Principal userDetails) {
        User user = getActiveUserByName(userDetails.getName());
        UserDtoForEditing userDto = modelMapper.map(user, UserDtoForEditing.class);
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
     *
     * @param user
     * @return userDto
     */
    public UserDto convertUserToDto(User user) {
        UserDto userDto = modelMapper.map(user, UserDto.class);
        // falls ein User ein CreatedAt oder UpdatedAt Value hat, muss der zu einem String konvertiert werden. Falls nicht darf das Date nicht konvertiert werden, sonst gibts NullPointerExceptions.
        if (user.getCreatedAt() != null)
            userDto.setCreatedAt(user.getCreatedAt(), ZonedDateTime.now(ZoneId.systemDefault()).toString()); //konvertiert Date zu String -> einfachhaltshaber nimmts immer die Zeitzone des Servers (ZoneId.systemdefault), vielleicht irgendwann mal durch die Zeitzone des Nutzers ersetzen.
        if (user.getUpdatedAt() != null)
            userDto.setUpdatedAt(user.getUpdatedAt(), ZonedDateTime.now(ZoneId.systemDefault()).toString());
        return userDto;
    }


    public boolean addUser(User user) {
        userRepository.save(user);
        return true;
    }

    public ResponseEntity registerUser(String name, String email, String password, Long dormId, MultipartFile profilePic) {

        Dorm dorm = dormService.getDormById(dormId).get();
        User newUser = new User(name, email, passwordEncoder.encode(password), dorm.getCity(), dorm);
        addUser(newUser);
        saveUserPic(profilePic, newUser.getId());
        return ResponseEntity.status(HttpStatus.OK).body("User erfolgreich angelegt.");
    }


    public User updateUser(User newUser, Long id) {
        Optional optional = userRepository.findById(id);

        if (optional.isPresent()) {
            User oldUser = (User) optional.get();
            if (newUser.getName() != null) oldUser.setName(newUser.getName());
            if (newUser.getEmail() != null) oldUser.setEmail(newUser.getEmail());
            if (newUser.getPassword() != null) oldUser.setPassword(newUser.getPassword());
            if (newUser.getProfilePic() != null) oldUser.setProfilePic(newUser.getProfilePic());
            if (newUser.getProducts() != null) oldUser.setProducts(newUser.getProducts());
            if (newUser.getDorm() != null) oldUser.setDorm(newUser.getDorm());
            if (newUser.getCity() != null) oldUser.setCity(newUser.getCity());


            userRepository.save(oldUser);
            System.out.println(oldUser);
            return oldUser;
        }
        return null;
    }

    public ResponseEntity saveUserPic(MultipartFile file, Long userId) {
        User user = getActiveUser(userId);                                      // We first load the user, for whom we wanna save the profile pic.
        if (user == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("user with Id" + userId + " doesn't exist in db.");    // Returns a status = 404 response
        if (user.getProfilePic() != null)
            deleteOldProfilePic(user);            // deletes old User Pic, so that there's always only one profile pic
        ResponseEntity response = imageService.storeImageS3(file.getOriginalFilename(), file, "user", user.getId());  //übergibt das Foto zum Speichern an imageService und gibt den Namen des Fotos zum gerade gespeicherten Foto zurück als Response Body. falls Speichern nicht geklappt hat kommt response mit Fehlercode zurück (400 oder ähnliches)
        if (response.getStatusCodeValue() == 200) {                         // if saving Pfoto was successfull => response status = 200...
            System.out.println("Unter folgendem Namen wurde das Foto lokal (src -> main -> resources -> images) gespeichert: " + file.getOriginalFilename());
            user.setProfilePic(file.getOriginalFilename());
            saveOrUpdateUser(user);                             //updated den User in der datenbank, damit der Fotoname da auch gespeichert ist.
        }
        return response;
    }

    private void deleteOldProfilePic(User user) {
        try {
            Resource resource = imageService.loadUserProfilePic(user.getProfilePic(), user);
            new File(resource.getURI()).delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public User getActiveUser(Long userId) {
        try {
            Optional<User> optionalEntity = getUserById(userId);
            return optionalEntity.get();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public User getActiveUserByName(String name) {
        return userRepository.findByName(name);
    }


    public UserDto getOwner(Long productId) {
        ProductDto product = productService.getProductById(productId);
        return getUserDtoById(product.getUserId());
    }

    public ResponseEntity validateRegistration(String name, String email, String password, Long dormId, MultipartFile profilePic) {
        if (name == null || name == "") {
            return new ResponseEntity("Username darf nicht leer sein.", HttpStatus.BAD_REQUEST);
        } else if (userRepository.existsByName(name)) {
            return new ResponseEntity("Username " + name + " bereits vergeben.", HttpStatus.BAD_REQUEST);
        } else if (email == null || email == "") {
            return new ResponseEntity("Email darf nicht leer sein.", HttpStatus.BAD_REQUEST);
        } else if (userRepository.existsByEmail(email)) {
            return new ResponseEntity("Email " + email + " bereits verwendet.", HttpStatus.BAD_REQUEST);
        } else if (password == null || password == "") {
            return new ResponseEntity("Passwort darf nicht leer sein.", HttpStatus.BAD_REQUEST);
        } else if (dormId == null) {
            return new ResponseEntity("Bitte ein Wohnheim auswählen.", HttpStatus.BAD_REQUEST);
        } else if (!dormService.existsById(dormId)) {
            return new ResponseEntity("Das ausgewählte Wohnheim existiert nicht in der Datenbank.", HttpStatus.BAD_REQUEST);
        } else if (profilePic != null) {
            if (!imageService.checkContentType(profilePic)) {
                return new ResponseEntity("Das Profilfoto muss vom Typ png, jpg, jpeg, bmp oder gif sein.", HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity(HttpStatus.OK);

    }



}