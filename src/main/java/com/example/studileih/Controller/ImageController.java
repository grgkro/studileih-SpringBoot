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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
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
            Optional<User> optionalEntity = userService.getUserById(Long.parseLong(userId));
            User user = optionalEntity.get();
            String imageName = null;
            try {
                imageName = imageService.store(file); //übergibt das Photo zum Speichern an imageService und gibt den Namen des Photos zum gerade gespeicherten Photo zurück
                System.out.println("Unter folgendem Namen wurde das Foto lokal (src -> main -> resources -> images) gespeichert: " + imageName);
            } catch (Exception e) {
                System.out.println("Error at groupController.handleFileUpload():" + e);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("a) image with same name was already uploaded - b) uploaded file was not an image - c) file size > 1048 KBs");
            }
            user.setProfilePic(imageName);  //verknüpft den Photonamen mit dem User, der es hochgeladen hat
            userService.saveOrUpdateUser(user); //updated den User in der datenbank, damit der Photoname da auch gespeichert ist.
        } else if (imgType.equals("productPic")) {
            //TODO: Product Fotos auch speicherbar machen (bisher gehen nur Userfotos)
            System.out.println("groupController.postImage-> groupPics are not programmed yet");
        } else if (imgType.equals("postPic")) {
            //TODO:
            //ich weiß grad auch nichtmehr, was ein postPic sein sollte... Vielleicht wenn etwas Offtopic geposted wird?
            System.out.println("groupController.postImage-> postPics are not programmed yet");
        }
        return ResponseEntity.status(HttpStatus.OK).body("Image was saved.");
    }

    @PostMapping("/loadProfilePicByUserId")
    public ResponseEntity<Resource> getImageByUserId(@RequestBody String userId) {
        // retrieve the file Name to the photo saved in the user database table
        Optional<User> optionalEntity = userService.getUserById(Long.parseLong(userId));
        User user = optionalEntity.get();
        String fileName = user.getProfilePic();
        // load the picture from the local storage
        Resource file = imageService.loadFile(fileName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")  //the Content-Disposition response header is a header indicating if the content is expected to be displayed inline in the browser, that is, as a Web page or as part of a Web page, or as an attachment, that is downloaded and saved locally.
                .body(file);
    }
}