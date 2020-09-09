package com.example.studileih.Service;

import com.example.studileih.Entity.Product;
import com.example.studileih.Entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ImageService {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    S3Services s3Services;

//    @Value("${file.location}")
//    private String location;

//    @PostConstruct
//    public void testString() {
//        System.out.println(location);
//    }

    // nach diesem Tutorial erstellt: https://grokonez.com/spring-framework/spring-boot/angular-6-upload-get-multipartfile-spring-boot-example

    // Logger is similar to system.out.println, but you can also see the outprint on a server-log (was useful for running on AWS Cloud)
    Logger log = LoggerFactory.getLogger(this.getClass().getName());

    // basePath is the root folder where you saved the project. imageFolderLocation completes the basePath to the image folder location
    private final String basePath = new File("").getAbsolutePath();
    private String parentFolderLocation = basePath + "/src/main/resources/images";

    /* --------------------- use this to store images in AWS S3 ---------------------- */
    public ResponseEntity storeImageS3(String keyName, MultipartFile file, String type, Long id) {
        s3Services.uploadFile(type + "s/" + type + id + "/" + keyName, file);
        return ResponseEntity.status(HttpStatus.OK).body("Bild " + keyName + " erfolgreich hochgeladen.");
    }


    /* --------------------- use this to store images locally -------------------------- */
    /**
     * The image gets send from the frontend as a Multipartfile. Here we save it.
     * There can't be two images in the image folder with the same name, so we return an error response if that happens.
     *
     * @param , type, id -> type can here either be "user" or "product". Depending on the type the photo will be saved as user profile pic or product pic
     * @return ResponseEntity
     */
//    public ResponseEntity storeImage(MultipartFile file, String type, Long id) {
//        createFolder(Paths.get(parentFolderLocation));   // falls "images" folder fehlt, erzeugen wir den:
//        createFolder(Paths.get(parentFolderLocation + "/" + type + "s"));  // falls "users" oder "products" folder fehlt, erzeugen wir den:
//        createFolder(Paths.get(parentFolderLocation + "/" + type + "s/" + type + id)); //creates a folder named "user + userId" oder "product" + productId. Only if folder doesn't already exists.
//
//        return storeImage(file, type + "Pic", Paths.get(parentFolderLocation + "/" + type + "s/" + type + id));   // jetzt können wir das Foto in den user1, user2, bzw. product1, product2,... folder speichern:
//
//    }

    public ResponseEntity loadProductPicByFilenameS3(String filename, Long productId) {
        // The file Names of the ProfilePics are saved in the user entities, so we first need to load the user from the user database table
        Product product = productService.getProductEntityById(productId);
        if (product == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product with Id" + productId + " doesn't exist in db.");    // Returns a status = 404 response
        // Then we load the picture from S3
        if (product.getPicPaths() != null && product.getPicPaths().contains(filename)) {
            byte[] bytearray = loadProductPicS3(filename, productId);  // Somehow you can't store the file directly in a variable of type File, instead you need to use a variable of type Resource.
            System.out.println(bytearray);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + "\"")  //the Content-Disposition response header is a header indicating if the content is expected to be displayed inline in the browser, that is, as a Web page or as part of a Web page, or as an attachment, that is downloaded and saved locally.
                    .body(bytearray);  // the response body now contains the profile pic
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product with userId = " + productId + "has no pictures yet or no picture with the given filename");  // Returns a status = 404 response
        }
    }

    public byte[] loadProductPicS3(String filename, Long productId) {
        byte[] bytearray = loadProductPicByFilenameAsResourceS3(filename, productId);
//        if (bytearray.exists() || bytearray.isReadable()) {
            return bytearray;
//        } else {
//            throw new RuntimeException("It seems like you deleted the images on the server, without also deleting them in the database ");
//        }
    }

    /*
     * returns a resource with the product pic.
     */
    public byte[] loadProductPicByFilenameAsResourceS3(String filename, Long id) {


            try {
                ByteArrayOutputStream baos = s3Services.downloadFile("products/product" + id + "/" + filename);
                System.out.println(baos);
                return baos.toByteArray();
//                Resource resource = null;
//                if (resource.exists() || resource.isReadable()) {
//                    return resource;
//                } else {
//                    throw new RuntimeException("It seems like you deleted the images on the server, without also deleting them in the database ");
//                }
            } catch (Exception e) {
                throw new RuntimeException("It seems like you deleted the images on the server, without deleting them in the database ");
            }

//        throw new RuntimeException("Unexpected error when loading the image in backend");
    }



    public ResponseEntity loadProductPicByFilename(String filename, Long productId) {
        // The file Names of the ProfilePics are saved in the user entities, so we first need to load the user from the user database table
        Product product = productService.getProductEntityById(productId);
        if (product == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("product with Id" + productId + " doesn't exist in db.");    // Returns a status = 404 response
        // Then we load the picture from the local storage
        if (product.getPicPaths() != null && product.getPicPaths().contains(filename)) {
            Resource file = loadProductPic(filename, productId);  // Somehow you can't store the file directly in a variable of type File, instead you need to use a variable of type Resource.
            System.out.println(file);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")  //the Content-Disposition response header is a header indicating if the content is expected to be displayed inline in the browser, that is, as a Web page or as part of a Web page, or as an attachment, that is downloaded and saved locally.
                    .body(file);  // the response body now contains the profile pic
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product with userId = " + productId + "has no pictures yet or no picture with the given filename");  // Returns a status = 404 response
        }
    }

    public ResponseEntity handleFileUpload(MultipartFile file, Long userId, Long productId, String imgType) {
        // -> if the image is a userPic -> update the user who posted it with the newly generated photo filePath of the just saved photo
        if (imgType.equals("userPic")) {
            return userService.saveUserPic(file, userId);
        } else if (imgType.equals("productPic")) {
            // before we store the image, we need to check if the image is already in the archive. If so, we need to delete it there. Otherwise it would be in the archive and in the normal folder at the same time. If you then delete it (transfer it from normal folder to archive, or restore it (transfer it from archive to normal folder) you would get a fileAlreadyExists Exeption.
            if (checkIfPicIsAlreadyInArchive(file, productId)) deletePicFromArchive(file, productId);
            Product product = productService.getProductEntityById(productId);                                      // We first load the product, for which we wanna save the pic.
            return saveProductPic(file, product);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Der imgType im Angular Code war falsch...");
    }

    public ResponseEntity archiveProductPicByFilename(String filename, String imgType, String productId) throws IOException {
        // find correct paths
        String parentFolderLocation = new File("").getAbsolutePath() + "/src/main/resources/images";
        String archiveFolderLocation = parentFolderLocation + "/archive/" + imgType + "s/" + imgType + productId;
        // create archive folder + sub folders (if doesn't exist yet)
        createFolder(Paths.get(parentFolderLocation));   // falls "images" folder fehlt, erzeugen wir den:
        createFolder(Paths.get(parentFolderLocation + "/archive"));  // falls "archive" folder fehlt, erzeugen wir den:
        createFolder(Paths.get(parentFolderLocation + "/archive/" + imgType + "s"));  // falls "users" oder "products" folder fehlt, erzeugen wir den:
        createFolder(Paths.get(archiveFolderLocation));  // falls der jeweilige User oder Product folder ("product1", "product2", ...) fehlt, erzeugen wir den:
        // copy the file to the archive folder
        copyFile(parentFolderLocation + "/" + imgType + "s/" + imgType + productId + "/" + filename, archiveFolderLocation + "/" + filename);
        // return success entity (OK - 200)
        return ResponseEntity.status(HttpStatus.OK).body(imgType + " erfolgreich archiviert");
    }

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
            deleteImageByFilename(filename, "archiveProductPic", productId);  // loadImageByFilename() returns a response with the product pic. If the image couldn't be loaded, the response will contain an error message
        }
        // return success entity (OK - 200)
        return ResponseEntity.status(HttpStatus.OK).body(imgType + " erfolgreich wiederhergestellt");
    }

    public static void copyFile(String from, String to) throws IOException {
        Path src = Paths.get(from);
        Path dest = Paths.get(to);
        Files.copy(src, dest);
    }

    public ResponseEntity deleteProductPicByFilename(String filename, Long productId) {
        // first remove the image from the databse
        Product product = productService.getProductEntityById(productId);
        if (product != null && product.getPicPaths() != null && product.getPicPaths().contains(filename)) {
            product.getPicPaths().remove(filename);
            productService.saveOrUpdateProduct(product);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ProductPic konnte in Datenbank nicht gefunden werden.");
        }
        // if the image was removed from DB, remove the image from the local storage, too:
        return deleteImageByFilename(filename, "productPic", productId);  // loadImageByFilename() returns a response with the product pic. If the image couldn't be loaded, the response will contain an error message

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

    public ResponseEntity getImageByUserId(Long userId) {
        // The file Names of the ProfilePics are saved in the user entities, so we first need to load the user from the user database table
        User user = userService.getActiveUser(userId);
        if (user == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("user with Id" + userId + " doesn't exist in db.");    // Returns a status = 404 response
        // Then we load the picture from the local storage
        if (user.getProfilePic() != null) {
            Resource file = loadUserProfilePic(user.getProfilePic(), user);  // Somehow you can't store the file directly in a variable of type File, instead you need to use a variable of type Resource.
            System.out.println(file);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")  //the Content-Disposition response header is a header indicating if the content is expected to be displayed inline in the browser, that is, as a Web page or as part of a Web page, or as an attachment, that is downloaded and saved locally.
                    .body(file);  // the response body now contains the profile pic
        } else {
            return ResponseEntity.status(HttpStatus.OK).body("User with userId = " + userId + "has no profilePic yet (user.getProfilePic() = null)");  // Returns a status = 404 response
        }
    }

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
            } catch (MalformedURLException e) {
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
        } else if (type.equals(("archiveProductPic"))) {
            Path imageFolderLocation = Paths.get(parentFolderLocation + "/archive/products/product" + id); // Each product has an own image folder. imageFolderLocation is the location of that folder.
            Path filePath = imageFolderLocation.resolve(filename);
            try {
                Resource resource = new UrlResource(filePath.toUri());
                if (resource.exists() || resource.isReadable()) {
                    return resource;
                } else {
                    throw new RuntimeException("It seems like you deleted the images in the archive on the server, without also deleting them in the database ");
                }
            } catch (Exception e) {
                throw new RuntimeException("It seems like you deleted the images in the archive on the server, without deleting them in the database ");
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

    public Boolean checkIfPicIsAlreadyInArchive(MultipartFile file, Long id) {
        try {
            loadImageByFilenameAsResource(file.getOriginalFilename(), "archiveProductPic", id);
            return true;
        } catch (Exception e) {
//            System.out.println(e);
            return false;
        }
    }

    public ResponseEntity saveProductPic(MultipartFile file, Product product) {

        if (product == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("product with Id" + product.getId() + " doesn't exist in db.");    // Returns a status = 404 response
        if (!hasAlreadyThisFile(file, product)) {                         // checks, if the Productalready has a Photo with exact same name.
            ResponseEntity response = storeImageS3(file.getOriginalFilename(), file, "product", product.getId());  //übergibt das Foto zum Speichern an imageService und gibt den Namen des Fotos zum gerade gespeicherten Foto zurück als Response Body. falls Speichern nicht geklappt hat kommt response mit Fehlercode zurück (400 oder ähnliches)
            if (response.getStatusCodeValue() == 200) {
                System.out.println("Unter folgendem Namen wurde das Foto lokal (src -> main -> resources -> images) gespeichert: " + file.getOriginalFilename()); // if saving photo was successfull => response status = 200...
                if (product.getPicPaths() == null) {                                   // we only saved the pic in the pic folder until now, not in the database. A Product can have multiple images, so the pic needs to get stored in a List
                    ArrayList<String> arr = new ArrayList<>();
                    //add the new picPath
                    arr.add(file.getOriginalFilename());
                    product.setPicPaths(arr);
                } else {
                    product.getPicPaths().add(file.getOriginalFilename());
                    product.setPicPaths(product.getPicPaths());
                }
                System.out.println(product.getPicPaths());
                productService.saveOrUpdateProduct(product);                             //updated das Product in der datenbank, damit der Fotoname da auch gespeichert ist.
            }
            return response;
        }
        return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body("Foto mit selbem Namen wurde für gleiches Produkt schonmal hochgeladen.");
    }

    private boolean hasAlreadyThisFile(MultipartFile file, Product product) {
        if (product.getPicPaths() != null) {
            Path path = Paths.get(file.getOriginalFilename());
            System.out.println("Path: " + path);
            if (product.getPicPaths().contains(path)) return true;
        }
        return false;
    }


    // delete img from archive
    public ResponseEntity deletePicFromArchive(MultipartFile file, Long id) {
        return deleteImageByFilename(file.getOriginalFilename(), "archiveProductPic", id);  // loadImageByFilename() returns a response with the product pic. If the image couldn't be loaded, the response will contain an error message
    }

    public Boolean checkContentType(MultipartFile profilePic) {
        String contentType = profilePic.getContentType();
        return contentType.equals("image/png") || contentType.equals("image/jpg") || contentType.equals("image/jpeg") || contentType.equals("image/bmp") || contentType.equals("image/gif");
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

