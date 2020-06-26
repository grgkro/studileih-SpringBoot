package com.example.studileih.Controller;

import com.example.studileih.Dto.ProductDto;
import com.example.studileih.Entity.Product;
import com.example.studileih.Entity.User;
import com.example.studileih.Service.ImageService;
import com.example.studileih.Service.ProductService;
import com.example.studileih.Service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.persistence.NoResultException;
import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@CrossOrigin
public class ImageController {

    @Autowired
    private ImageService imageService;

    @Autowired
    private UserService userService;

    /*
     * method for posting images into the image folder (src -> main -> resources -> images) and put the filePath into the database.
     */
    @PostMapping("/postImage")
    public ResponseEntity handleFileUpload(@RequestParam("file") MultipartFile file, String userId, String groupId, String postId, String imgType) {
        // -> if the image is a userPic -> update the user who posted it with the newly generated photo filePath of the just saved photo
        if (imgType.equals("userPic")) {
            return saveUserPic(file, userId, groupId, postId, imgType);
        } else if (imgType.equals("productPic")) {
            //TODO: Product Fotos auch speicherbar machen (bisher gehen nur Userfotos)
            System.out.println("groupController.postImage-> groupPics are not programmed yet");
        } else if (imgType.equals("postPic")) {
            //TODO:
            //ich weiß grad auch nichtmehr, was ein postPic sein sollte... Vielleicht wenn etwas Offtopic geposted wird?
            System.out.println("groupController.postImage-> postPics are not programmed yet");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Der imgType im Angular Code war falsch...");
    }

    private ResponseEntity saveUserPic(MultipartFile file, String userId, String groupId, String postId, String imgType) {
        User user = getActiveUser(userId);                                      // We first load the user, for whom we wann save the profile pic.
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("user with Id" + userId + " doesn't exist in db.");    // Returns a status = 404 response
        if (!hasUserAlreadySavedThisFile(file, user)) {                         // checks, if User is currently using a Photo with exact same name as ProfilePic
            ResponseEntity response = imageService.storeUserImage(file, user);  //übergibt das Foto zum Speichern an imageService und gibt den Namen des Fotos zum gerade gespeicherten Foto zurück als Response Body. falls Speichern nicht geklappt hat kommt response mit Fehlercode zurück (400 oder ähnliches)
            if (response.getStatusCodeValue() == 200) {                         // if saving Pfoto was successfull => response status = 200...
                System.out.println("Unter folgendem Namen wurde das Foto lokal (src -> main -> resources -> images) gespeichert: " + file.getOriginalFilename());
                user.setProfilePic(file.getOriginalFilename());
                userService.saveOrUpdateUser(user);                             //updated den User in der datenbank, damit der Fotoname da auch gespeichert ist.
            }
            return response;
        }
        return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body("Foto mit selbem Namen wurde vom gleichen User schonmal hochgeladen.");
    }

    private boolean hasUserAlreadySavedThisFile(MultipartFile file, User user) {
        if (user.getProfilePic() != null) {
            if (user.getProfilePic().equals(file.getOriginalFilename())) return true;
            deleteOldProfilePic(user);
        }
        return false;
    }

    private void deleteOldProfilePic(User user) {
        try {
            Resource resource = imageService.loadFile(user.getProfilePic(), user);
            imageService.deleteImage(new File(resource.getURI()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    *
     */
    @PostMapping("/loadProfilePicByUserId")
    public ResponseEntity getImageByUserId(@RequestBody String userId) {
        // The file Names of the ProfilePics are saved in the user entities, so we first need to load the user from the user database table
        User user = getActiveUser(userId);
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("user with Id" + userId + " doesn't exist in db.");    // Returns a status = 404 response
        // Then we load the picture from the local storage
        if (user.getProfilePic() != null) {
            Resource file = imageService.loadFile(user.getProfilePic(), user);  // Somehow you can't store the file directly in a variable of type File, instead you need to use a variable of type Resource.
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")  //the Content-Disposition response header is a header indicating if the content is expected to be displayed inline in the browser, that is, as a Web page or as part of a Web page, or as an attachment, that is downloaded and saved locally.
                    .body(file);  // the response body now contains the profile pic
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with userId = " + userId + "has no profilePic yet (user.getProfilePic() = null)");  // Returns a status = 404 response
        }

    }

    public User getActiveUser(String userId) {
        try {
            Optional<User> optionalEntity = userService.getUserById(Long.parseLong(userId));
            return optionalEntity.get();
        } catch (NoSuchElementException e) {
            return null;
        }
    }
}