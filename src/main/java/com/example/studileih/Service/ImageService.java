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
     *
     * @param file, type, id -> type can here either be "user" or "product". Depending on the type the photo will be saved as user profile pic or product pic
     * @return ResponseEntity
     */
    public ResponseEntity storeImage(MultipartFile file, String type, Long id) {
        createFolder(Paths.get(parentFolderLocation));   // falls "images" folder fehlt, erzeugen wir den:
        createFolder(Paths.get(parentFolderLocation + "/" + type + "s"));  // falls "users" folder fehlt, erzeugen wir den:
        createFolder(Paths.get(parentFolderLocation + "/" + type + "s/" + type + id)); //creates a folder named "user + userId". Only if folder doesn't already exists.
        return storeImage(file, type + "Pic", Paths.get(parentFolderLocation + "/" + type + "s/" + type + id));   // jetzt können wir das Foto in den user1, user2,... folder speichern:

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

//    public ResponseEntity storeFile(File file, String type, Path imageFolderLocation) {
//        if (type.equals("userPic")) {
//            try {
//                Files.copy(file.getInputStream(), imageFolderLocation.resolve(file.getOriginalFilename()));  // this line saves the image at the provided path (not in the database)
//            } catch (Exception e) {
//                System.out.println("Error at imageService:" + e);
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("a) user image with same name was already uploaded - b) uploaded file was not an image - c) file size > 500KBs");
//            }
//            return ResponseEntity.status(HttpStatus.OK).body("Dein Profilfoto wurde gespeichert.");
//        } else if (type.equals(("productPic"))) {
//            try {
//                Files.copy(file.getInputStream(), imageFolderLocation.resolve(file.getOriginalFilename())); // this line saves the image at the provided path (not in the database)
//            } catch (Exception e) {
//                System.out.println("Error at imageService:" + e);
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("a) Product image with same name was already uploaded - b) uploaded file was not an image - c) file size > 500KBs");
//            }
//            return ResponseEntity.status(HttpStatus.OK).body("Das Foto wurde zu deinem Produkt gespeichert.");
//        }
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unerwarteter Fehler");
//    }

    public Resource loadUserProfilePic(String filename, User user) {
        Resource resource = loadImageByFilenameAsResource(filename, "userPic", user.getId());
        if (resource.exists() || resource.isReadable()) {
            return resource;
        } else {
            throw new RuntimeException("It seems like you deleted the images on the server, without also deleting them in the database ");
        }
    }


    /*
     * returns a response with the product pic. If the image couldn't be loaded, the response will contain an error message
     */
    public Resource loadProductPic(String filename, Long productId) {
        Resource resource = loadImageByFilenameAsResource(filename, "productPic", productId);
        if (resource.exists() || resource.isReadable()) {
            return resource;
        } else {
            throw new RuntimeException("It seems like you deleted the images on the server, without also deleting them in the database ");
        }
    }

    /*
     * returns a resource with the product pic.
     */
    public Resource loadImageByFilenameAsResource(String filename, String type, Long id) {
        if (type.equals("userPic")) {
                Path imageFolderLocation = Paths.get(parentFolderLocation + "/users/user" + id);
                Path filePath = imageFolderLocation.resolve(filename);
            try {
                Resource resource = new UrlResource(filePath.toUri());
                if (resource.exists() || resource.isReadable()) {
                    return resource;
                } else {
                    throw new RuntimeException("It seems like you deleted the images on the server, without also deleting them in the database ");
                }
            }catch (MalformedURLException e) {
                throw new RuntimeException("It seems like you deleted the images on the server, without deleting them in the database ");
            }
        } else if (type.equals(("productPic"))) {
                Path imageFolderLocation = Paths.get(parentFolderLocation + "/products/product" + id); // Each product has an own image folder. imageFolderLocation is the location of that folder.
                Path filePath = imageFolderLocation.resolve(filename);
                try {
                    Resource resource = new UrlResource(filePath.toUri());
                    if (resource.exists() || resource.isReadable()) {
                        return resource;
                    } else {
                        throw new RuntimeException("It seems like you deleted the images on the server, without also deleting them in the database ");
                    }
            } catch (Exception e) {
                    throw new RuntimeException("It seems like you deleted the images on the server, without deleting them in the database ");
            }
        }
        throw new RuntimeException("Unexpected error when loading the image in backend");
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


    public void createFolder(Path FolderLocation) {
        if (Files.notExists(FolderLocation)) {
            try {
                Files.createDirectory(FolderLocation);
            } catch (IOException e) {
                throw new RuntimeException("Could not initialize storage!");
            }
        }
    }

    public ResponseEntity deleteImageByFilename(String filename, String type, long id) {
        Boolean successfullyDeleted;
            try {
                Resource resource = loadImageByFilenameAsResource(filename, type, id);
                successfullyDeleted = new File(resource.getURI()).delete();
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
            }
        return ResponseEntity.status(HttpStatus.OK).body(successfullyDeleted);

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

