package com.example.studileih.Controller;

import com.example.studileih.Entity.Product;
import com.example.studileih.Entity.User;
import com.example.studileih.Service.ImageService;
import com.example.studileih.Service.ProductService;
import com.example.studileih.Service.UserService;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiResponse;


@RestController
@CrossOrigin
@Api(tags = "Images API - controller methods for managing Images")
public class ImageController {

    @Autowired
    private ImageService imageService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    /*
     * loads a product pic
     */
    @PostMapping("/images/loadProductPicByFilename")    //https://bezkoder.com/spring-boot-upload-multiple-files/
    @ApiOperation(value = "Loads and returns the image of a product by it's provided filename")
    public ResponseEntity loadProductPicByFilename(@RequestParam("filename") String filename, String productId) {
        // The file Names of the ProfilePics are saved in the user entities, so we first need to load the user from the user database table
        Product product = imageService.getProduct(productId);
        if (product == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("product with Id" + productId + " doesn't exist in db.");    // Returns a status = 404 response
        // Then we load the picture from the local storage
        if (product.getPicPaths() != null && product.getPicPaths().contains(filename)) {
            Resource file = imageService.loadProductPic(filename, Long.parseLong(productId));  // Somehow you can't store the file directly in a variable of type File, instead you need to use a variable of type Resource.
            System.out.println(file);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")  //the Content-Disposition response header is a header indicating if the content is expected to be displayed inline in the browser, that is, as a Web page or as part of a Web page, or as an attachment, that is downloaded and saved locally.
                    .body(file);  // the response body now contains the profile pic
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product with userId = " + productId + "has no pictures yet or no picture with the given filename");  // Returns a status = 404 response

        }
    }

    @PostMapping("/postImage")
    @ApiOperation(value = "Add new Image")
    public ResponseEntity handleFileUpload (@RequestParam("file") MultipartFile file, String userId, String productId, String imgType){
        // -> if the image is a userPic -> update the user who posted it with the newly generated photo filePath of the just saved photo
        if (imgType.equals("userPic")) {
            return saveUserPic(file, userId);
        } else if (imgType.equals("productPic")) {
            // before we store the image, we need to check if the image is already in the archive. If so, we need to delete it there. Otherwise it would be in the archive and in the normal folder at the same time. If you then delete it (transfer it from normal folder to archive, or restore it (transfer it from archive to normal folder) you would get a fileAlreadyExists Exeption.
            if (imageService.checkIfPicIsAlreadyInArchive(file, Long.parseLong(productId))) imageService.deletePicFromArchive( file,  Long.parseLong(productId));
            return imageService.saveProductPic(file, productId);
        } else if (imgType.equals("newProduct")) {

        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Der imgType im Angular Code war falsch...");
    }


    /*
     * archiviert ein user oder product pic, falls sich der Nutzer nach Löschen auf "Rückgängig" klickt.
     * Ob das imag als user oder product pic abgespeichert wird, hängt vom imgType ab ("product" oder "user")
     */
    @PostMapping("/images/archivePicByFilename")
    @ApiOperation(value = "Transfers an deleted image from the user or product folder to the archive folder, so that it can be restored later.")
    public ResponseEntity archiveProductPicByFilename(@RequestParam("filename") String filename, String imgType, String productId) throws IOException {
        // find correct paths
        String parentFolderLocation = new File("").getAbsolutePath() + "/src/main/resources/images";
        String archiveFolderLocation = parentFolderLocation + "/archive/" + imgType + "s/" + imgType + productId;
        // create archive folder + sub folders (if doesn't exist yet)
        imageService.createFolder(Paths.get(parentFolderLocation));   // falls "images" folder fehlt, erzeugen wir den:
        imageService.createFolder(Paths.get(parentFolderLocation + "/archive"));  // falls "archive" folder fehlt, erzeugen wir den:
        imageService.createFolder(Paths.get(parentFolderLocation + "/archive/" + imgType + "s"));  // falls "users" oder "products" folder fehlt, erzeugen wir den:
        imageService.createFolder(Paths.get(archiveFolderLocation));  // falls der jeweilige User oder Product folder ("product1", "product2", ...) fehlt, erzeugen wir den:
        // copy the file to the archive folder
        copyFile(parentFolderLocation + "/" + imgType + "s/" + imgType + productId + "/" + filename, archiveFolderLocation + "/" + filename);
        // return success entity (OK - 200)
        return ResponseEntity.status(HttpStatus.OK).body(imgType + " erfolgreich archiviert");
    }

    public static void copyFile(String from, String to) throws IOException {
        Path src = Paths.get(from);
        Path dest = Paths.get(to);
        Files.copy(src, dest);
    }


    /*
     * gets an archived product pic from the archive and restores it to the local storage and the database
     */
    @PostMapping("/images/restorePicByFilename")
    @ApiOperation(value = "Restores a deleted image from the archive to the user or product folder")
    public ResponseEntity restorePicByFilename(@RequestParam("filename") String filename, String imgType, String productId) throws IOException {
        // find correct paths
        String parentFolderLocation = new File("").getAbsolutePath() + "/src/main/resources/images";
        String targetFolderLocation = parentFolderLocation + "/" + imgType + "s/" + imgType + productId + "/";
        String archiveFolderLocation = parentFolderLocation + "/archive/" + imgType + "s/" + imgType + productId + "/";
        // copy the file from the archive folder to the parent folder

        copyFile(archiveFolderLocation + filename, targetFolderLocation + filename);

        // load product or user and readd the restored img to picPaths
        if (imgType.equals("product")) {
            Product product = imageService.getProduct(productId);
            if (product == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("product with Id" + productId + " doesn't exist in db.");    // Returns a status = 404 response
            // Then we add the picture to the picPaths
            if (!product.getPicPaths().contains(filename)) {
                ArrayList<String> picPaths = product.getPicPaths();
                picPaths.add(filename);
                product.setPicPaths(picPaths);
                productService.saveOrUpdateProduct(product);
            }
            // delete restored img from archive
            imageService.deleteImageByFilename(filename, "archiveProductPic", Long.parseLong(productId));  // loadImageByFilename() returns a response with the product pic. If the image couldn't be loaded, the response will contain an error message
        }
        // return success entity (OK - 200)
        return ResponseEntity.status(HttpStatus.OK).body(imgType + " erfolgreich wiederhergestellt");
    }

    /*
     * deletes the archive of one product or user (depending on the provided archiveType)
     */
    @PostMapping("/images/deleteArchive")
    @ApiOperation(value = "Deletes the archive folder of one user or product")
    public ResponseEntity deleteArchive(@RequestParam("archiveType") String archiveType, String id) throws IOException {
        String parentFolderLocation = new File("").getAbsolutePath() + "/src/main/resources/images";
        String archiveFolderLocation = parentFolderLocation + "/archive/" + archiveType + "s/" + archiveType + id;
        File file = new File(archiveFolderLocation);
        FileUtils.deleteDirectory(file);
        // return success entity (OK - 200)
        return ResponseEntity.status(HttpStatus.OK).body(archiveType + " Archiv erfolgreich gelöscht");
    }

    /*
     * deletes the imageFolder of one product or user (depending on the provided folderType)
     */
    @PostMapping("/images/deleteImageFolder")
    @ApiOperation(value = "Deletes the imageFolder of one product or user (depending on the provided folderType)")
    public ResponseEntity deleteImageFolder(@RequestParam("folderType") String folderType, String id) throws IOException {
        String parentFolderLocation = new File("").getAbsolutePath() + "/src/main/resources/images";
        String archiveFolderLocation = parentFolderLocation + "/" + folderType + "s/" + folderType + id;
        File file = new File(archiveFolderLocation);
        FileUtils.deleteDirectory(file);
        // return success entity (OK - 200)
        return ResponseEntity.status(HttpStatus.OK).body(folderType + " Bilder erfolgreich gelöscht");
    }




        /*
         * deletes a product image by filename
         */
        @PostMapping("/images/deleteProductPicByFilename")
        @ApiOperation(value = "Deletes a product image by filename")
        public ResponseEntity deleteProductPicByFilename (@RequestParam("filename") String filename, String productId){
            // first remove the image from the databse
            Optional<Product> optional = productService.getProductById(Long.parseLong(productId));   // the id always comes as a string from angular, even when you send it as a number in angular...
            Product product = optional.get();
            if (product != null && product.getPicPaths() != null && product.getPicPaths().contains(filename)) {
                product.getPicPaths().remove(filename);
                productService.saveOrUpdateProduct(product);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ProductPic konnte in Datenbank nicht gefunden werden.");
            }
            // if the image was removed from DB, remove the image from the local storage, too:
            return imageService.deleteImageByFilename(filename, "productPic", Long.parseLong(productId));  // loadImageByFilename() returns a response with the product pic. If the image couldn't be loaded, the response will contain an error message
        }






    @PostMapping("/loadProfilePicByUserId")
    @ApiOperation(value = "Add new Profile Image to User identified by ID")
    public ResponseEntity getImageByUserId (@RequestBody Long userId){
        // The file Names of the ProfilePics are saved in the user entities, so we first need to load the user from the user database table
        User user = getActiveUser(userId);
        if (user == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("user with Id" + userId + " doesn't exist in db.");    // Returns a status = 404 response
        // Then we load the picture from the local storage
        if (user.getProfilePic() != null) {
            Resource file = imageService.loadUserProfilePic(user.getProfilePic(), user);  // Somehow you can't store the file directly in a variable of type File, instead you need to use a variable of type Resource.
            System.out.println(file);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")  //the Content-Disposition response header is a header indicating if the content is expected to be displayed inline in the browser, that is, as a Web page or as part of a Web page, or as an attachment, that is downloaded and saved locally.
                    .body(file);  // the response body now contains the profile pic
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with userId = " + userId + "has no profilePic yet (user.getProfilePic() = null)");  // Returns a status = 404 response
        }

    }

        private ResponseEntity saveUserPic (MultipartFile file, String userId){
            User user = getActiveUser(Long.parseLong(userId));                                      // We first load the user, for whom we wann save the profile pic.
            if (user == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("user with Id" + userId + " doesn't exist in db.");    // Returns a status = 404 response
            if (user.getProfilePic() != null)
                deleteOldProfilePic(user);            // deletes old User Pic, so that there's always only one profile pic
            ResponseEntity response = imageService.storeImage(file, "user", user.getId());  //übergibt das Foto zum Speichern an imageService und gibt den Namen des Fotos zum gerade gespeicherten Foto zurück als Response Body. falls Speichern nicht geklappt hat kommt response mit Fehlercode zurück (400 oder ähnliches)
            if (response.getStatusCodeValue() == 200) {                         // if saving Pfoto was successfull => response status = 200...
                System.out.println("Unter folgendem Namen wurde das Foto lokal (src -> main -> resources -> images) gespeichert: " + file.getOriginalFilename());
                user.setProfilePic(file.getOriginalFilename());
                userService.saveOrUpdateUser(user);                             //updated den User in der datenbank, damit der Fotoname da auch gespeichert ist.
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
                Optional<User> optionalEntity = userService.getUserById(userId);
                return optionalEntity.get();
            } catch (NoSuchElementException e) {
                return null;
            }
        }




    }