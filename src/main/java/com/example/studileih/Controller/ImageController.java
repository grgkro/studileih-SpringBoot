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
    public ResponseEntity loadProductPicByFilename(String filename, Long productId) {
        // The file Names of the ProfilePics are saved in the user entities, so we first need to load the user from the user database table
        Product product = productService.getProductEntityById(productId);
        if (product == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("product with Id" + productId + " doesn't exist in db.");    // Returns a status = 404 response
        // Then we load the picture from the local storage
        if (product.getPicPaths() != null && product.getPicPaths().contains(filename)) {
            Resource file = imageService.loadProductPic(filename, productId);  // Somehow you can't store the file directly in a variable of type File, instead you need to use a variable of type Resource.
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
    public ResponseEntity handleFileUpload (MultipartFile file, Long userId, Long productId, String imgType){
        // -> if the image is a userPic -> update the user who posted it with the newly generated photo filePath of the just saved photo
        if (imgType.equals("userPic")) {
            return userService.saveUserPic(file, userId);
        } else if (imgType.equals("productPic")) {
            // before we store the image, we need to check if the image is already in the archive. If so, we need to delete it there. Otherwise it would be in the archive and in the normal folder at the same time. If you then delete it (transfer it from normal folder to archive, or restore it (transfer it from archive to normal folder) you would get a fileAlreadyExists Exeption.
            if (imageService.checkIfPicIsAlreadyInArchive(file, productId)) imageService.deletePicFromArchive( file,  productId);
            Product product = productService.getProductEntityById(productId);                                      // We first load the product, for which we wanna save the pic.
            return imageService.saveProductPic(file, product);
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
    public ResponseEntity archiveProductPicByFilename( String filename, String imgType, String productId) throws IOException {
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
    public ResponseEntity restorePicByFilename(String filename, String imgType, Long productId) throws IOException {
        // find correct paths
        String parentFolderLocation = new File("").getAbsolutePath() + "/src/main/resources/images";
        String targetFolderLocation = parentFolderLocation + "/" + imgType + "s/" + imgType + productId + "/";
        String archiveFolderLocation = parentFolderLocation + "/archive/" + imgType + "s/" + imgType + productId + "/";
        // copy the file from the archive folder to the parent folder

        copyFile(archiveFolderLocation + filename, targetFolderLocation + filename);

        // load product or user and readd the restored img to picPaths
        if (imgType.equals("product")) {
            Product product = productService.getProductEntityById(productId);
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
            imageService.deleteImageByFilename(filename, "archiveProductPic", productId);  // loadImageByFilename() returns a response with the product pic. If the image couldn't be loaded, the response will contain an error message
        }
        // return success entity (OK - 200)
        return ResponseEntity.status(HttpStatus.OK).body(imgType + " erfolgreich wiederhergestellt");
    }

    /*
     * deletes the archive of one product or user (depending on the provided archiveType)
     */
    @PostMapping("/images/deleteArchive")
    @ApiOperation(value = "Deletes the archive folder of one user or product")
    public ResponseEntity deleteArchive(String archiveType, String id) throws IOException {
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
    public ResponseEntity deleteImageFolder(String folderType, String id) throws IOException {
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
        public ResponseEntity deleteProductPicByFilename (String filename, Long productId){
            // first remove the image from the databse
            Product product = productService.getProductEntityById(productId);
            if (product != null && product.getPicPaths() != null && product.getPicPaths().contains(filename)) {
                product.getPicPaths().remove(filename);
                productService.saveOrUpdateProduct(product);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ProductPic konnte in Datenbank nicht gefunden werden.");
            }
            // if the image was removed from DB, remove the image from the local storage, too:
            return imageService.deleteImageByFilename(filename, "productPic", productId);  // loadImageByFilename() returns a response with the product pic. If the image couldn't be loaded, the response will contain an error message
        }






    @PostMapping("/loadProfilePicByUserId")
    @ApiOperation(value = "Add new Profile Image to User identified by ID")
    public ResponseEntity getImageByUserId (@RequestBody Long userId){
        // The file Names of the ProfilePics are saved in the user entities, so we first need to load the user from the user database table
        User user = userService.getActiveUser(userId);
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
            return ResponseEntity.status(HttpStatus.OK).body("User with userId = " + userId + "has no profilePic yet (user.getProfilePic() = null)");  // Returns a status = 404 response
        }

    }






    }