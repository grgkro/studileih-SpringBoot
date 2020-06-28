package com.example.studileih.Service;

import com.example.studileih.Entity.Product;
import com.example.studileih.Entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
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
        try {
            String tempUserFolderLocation = parentFolderLocation  + "/users/user" + user.getId();
            Path imageFolderLocation = Paths.get(tempUserFolderLocation);
            createImageFolder(imageFolderLocation); //creates a folder named "user + userId". Only if folder doesn't already exists.
            Files.copy(file.getInputStream(), imageFolderLocation.resolve(file.getOriginalFilename()));
        } catch (Exception e) {
            System.out.println("Error at imageService:" + e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("a) image with same name was already uploaded - b) uploaded file was not an image - c) file size > 500KBs");
        }
        return ResponseEntity.status(HttpStatus.OK).body("Dein Foto wurde gespeichert.");
    }

    /**
     * The image gets send from the frontend as a Multipartfile. Here we save it.
     * There can't be two images in the image folder with the same name, so we return an error response if that happens.
     * @param file
     * @return ResponseEntity
     */
    public ResponseEntity storeProductImage(MultipartFile file, Product product) {
        try {
            Path imageFolderLocation = Paths.get(parentFolderLocation + "/products/product" + product.getId());
            createImageFolder(imageFolderLocation);    //creates a new folder named "product + productId". Only if folder doesn't already exists.
            Files.copy(file.getInputStream(), imageFolderLocation.resolve(file.getOriginalFilename()));
        } catch (Exception e) {
            System.out.println("Error at imageService:" + e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("a) image with same name was already uploaded - b) uploaded file was not an image - c) file size > 500KBs");
        }
        return ResponseEntity.status(HttpStatus.OK).body("Dein Foto wurde gespeichert.");
    }

    public Resource loadUserProfilePic(String filename, User user) {
        try {
            String tempUserFolderLocation = parentFolderLocation  + "/users/user" + user.getId();
            Path imageFolderLocation = Paths.get(tempUserFolderLocation);
            Path file = imageFolderLocation.resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("It seems like you deleted the images on the server, without also deleting them in the database ");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("It seems like you deleted the images on the server, without deleting them in the database ");
        }
    }

    // wird noch nicht benutzt
//    public void deleteAll() {
//        FileSystemUtils.deleteRecursively(imageFolderLocation.toFile());
//    }

    public void deleteImage(File file) {
        if(file.delete()) // deletes the file already in the if statement and returns boolean (that we don't use)
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

    public List<List> loadProductPics() {
        // get all products from database
        List<Product> allProducts = productService.listAllProducts();
        // for each product get all pics (Each product can have multiple pics) and add them to a List
        List<Resource> listOfAllPicsOfOneProduct;
        List<List> listOfAllPics = new ArrayList<>();
        for (Product product: allProducts) {
            if (product.getPicPaths() != null) {
                listOfAllPicsOfOneProduct = loadProductPicsById(product.getId());
                listOfAllPics.add(listOfAllPicsOfOneProduct);
            }
        }
        return listOfAllPics;
    }

    public List<Resource> loadProductPicsById(Long productId) {
        try {
            String tempProductFolderLocation = parentFolderLocation  + "/products/product" + productId;
            Path imageFolderLocation = Paths.get(tempProductFolderLocation);
            return listFilesForFolder(imageFolderLocation);
        } catch (MalformedURLException e) {
            throw new RuntimeException("It seems like you deleted the images on the server, without deleting them in the database ");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Something went wrong while loading the pictures for each product");
        }
    }

    public List<Resource> listFilesForFolder(final Path folderLocation) throws IOException {
        try (Stream<Path> paths = Files.walk(Paths.get(String.valueOf(folderLocation)))) {
            return paths
                        .filter(Files::isRegularFile)
                        .map(Path::toUri)
                        .map(uri -> {
                            try {
                                return new UrlResource(uri);
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                                return null;
                            }
                        })
                        .filter(Resource::isReadable)
                        .collect(Collectors.toList());

        }
    }
}



