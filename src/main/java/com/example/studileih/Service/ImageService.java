package com.example.studileih.Service;

import com.example.studileih.Entity.Product;
import com.example.studileih.Entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ImageService {

    @Autowired
    private ProductService productService;

    // nach diesem Tutorial erstellt: https://grokonez.com/spring-framework/spring-boot/angular-6-upload-get-multipartfile-spring-boot-example

    // Logger is similar to system.out.println, but you can also see the outprint on a server-log (was useful for running on AWS Cloud)
    Logger log = LoggerFactory.getLogger(this.getClass().getName());

    // basePath is the root folder where you saved the project. imageFolderLocation completes the basePath to the image folder location
    private final String basePath = new File("").getAbsolutePath();
    private String parentFolderLocation = basePath + "/src/main/resources/images";

    /**
     * The image gets send from the frontend as a Multipartfile. Here we save it.
     * There can't be two images in the image folder with the same name, so we return an error response if that happens.
     * @param file
     * @return ResponseEntity  
     */
    public ResponseEntity storeUserImage(MultipartFile file, User user) {
        Path imageFolderLocation = Paths.get(parentFolderLocation  + "/users/user" + user.getId());
        createImageFolder(imageFolderLocation); //creates a folder named "user + userId". Only if folder doesn't already exists.
        return storeImage(file, "userPic", imageFolderLocation);
    }

    /**
     * The image gets send from the frontend as a Multipartfile. Here we save it.
     * There can't be two images in the image folder with the same name, so we return an error response if that happens.
     * @param file
     * @return ResponseEntity
     */
    public ResponseEntity storeProductImage(MultipartFile file, Product product) {
        Path imageFolderLocation = Paths.get(parentFolderLocation + "/products/product" + product.getId());
        createImageFolder(imageFolderLocation);    //creates a new folder named "product + productId". Only if folder doesn't already exists.
        return storeImage(file, "productPic", imageFolderLocation);
    }

    public ResponseEntity storeImage(MultipartFile file, String type, Path imageFolderLocation) {
        if (type.equals("userPic")) {
            try {
                Files.copy(file.getInputStream(), imageFolderLocation.resolve(file.getOriginalFilename()));  // this line saves the image at the provided path (not in the database)
            } catch (Exception e) {
                System.out.println("Error at imageService:" + e);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("a) user image with same name was already uploaded - b) uploaded file was not an image - c) file size > 500KBs");
            }
            return ResponseEntity.status(HttpStatus.OK).body("Dein Profilfoto wurde gespeichert.");
        } else if (type.equals(("productPic"))) {
            try {
                Files.copy(file.getInputStream(), imageFolderLocation.resolve(file.getOriginalFilename())); // this line saves the image at the provided path (not in the database)
            } catch (Exception e) {
                System.out.println("Error at imageService:" + e);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("a) Product image with same name was already uploaded - b) uploaded file was not an image - c) file size > 500KBs");
            }
            return ResponseEntity.status(HttpStatus.OK).body("Das Foto wurde zu deinem Produkt gespeichert.");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unerwarteter Fehler");
     }

    public Resource loadUserProfilePic(String filename, User user) {
            Path imageFolderLocation = Paths.get(parentFolderLocation  + "/users/user" + user.getId());
            Path filePath = imageFolderLocation.resolve(filename);
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("It seems like you deleted the images on the server, without also deleting them in the database ");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("It seems like you deleted the images on the server, without deleting them in the database ");
        }
    }

    /*
     * returns a response with the product pic. If the image couldn't be loaded, the response will contain an error message
     */
    public ResponseEntity loadImageByFilename(String filename, Long productId) {
        Path imageFolderLocation = Paths.get(parentFolderLocation  + "/products/product" + productId); // Each product has an own image folder. imageFolderLocation is the location of that folder.
        Path filePath = imageFolderLocation.resolve(filename);
        return loadFile(filePath);
    }

    // This is the function that really loads the image from the local storage
    public ResponseEntity loadFile(Path filePath) {
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"").body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Foto existiert nicht oder konnte nicht gelesen werden.");
            }
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + ex);
        }
    }

    // wird noch nicht benutzt
//    public void deleteAll() {
//        FileSystemUtils.deleteRecursively(imageFolderLocation.toFile());
//    }

    public void deleteImage(File file) {
        if(file.delete()) // the command in the if statement deletes the file already in the if statement and returns boolean (which we don't use)
        {
            System.out.println("Old User profile pic was deleted successfully");
        }
        else
        {
            System.out.println("Failed to delete the old User profile pic");
        }
    }

    public void createImageFolder(Path imageFolderLocation) {
        if(Files.notExists(imageFolderLocation)) {
            try {
                Files.createDirectory(imageFolderLocation);
            } catch (IOException e) {
                throw new RuntimeException("Could not initialize storage!");
            }
        }
    }

}




//    public List<ResponseEntity<UrlResource>> listFilesForFolder(final Path folderLocation) throws IOException {              // https://stackoverflow.com/questions/1844688/how-to-read-all-files-in-a-folder-from-java
//        try (Stream<Path> paths = Files.walk(Paths.get(String.valueOf(folderLocation)))) {
//            return paths
//                        .filter(Files::isRegularFile)
//                        .map(Path::toUri)
//                        .map(uri -> {
//                            try {
//                                return new UrlResource(uri);
//                            } catch (MalformedURLException e) {
//                                e.printStackTrace();
//                                return null;
//                            }
//                        })
//                        .filter(Resource::isReadable)
//                        .map(urlResource -> ResponseEntity.ok()
//                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; all product pics")  //the Content-Disposition response header is a header indicating if the content is expected to be displayed inline in the browser, that is, as a Web page or as part of a Web page, or as an attachment, that is downloaded and saved locally.
//                            .body(urlResource) )
//                        .collect(Collectors.toList());
//        }
//    }

