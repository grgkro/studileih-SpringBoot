package com.example.studileih.Service;

import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.example.studileih.Entity.Product;
import com.example.studileih.Entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

@Service
public class S3ImageArchiveAndDeleteService {

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

    public ResponseEntity archivePicByFilename(String filename, String imgType, String id) {
        // create correct paths
        String from = imgType + "s/" + imgType + id + "/" + filename ;
        String to = "archive/" + imgType + "s/" + imgType + id + "/" + filename;
        //check if "from" file exists
        if (!hasFile(from)) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(filename + " konnte nicht gefunden werden.");
        //check if "to" file exists
        if (hasFile(to)) return ResponseEntity.status(HttpStatus.CONFLICT).body(filename + " existiert bereits.");
        // then copy the file
        s3Services.copyFile(from, to);
        // return success entity (OK - 200)
        return ResponseEntity.status(HttpStatus.OK).body(filename + " erfolgreich archiviert.");
    }

    // not tested yet
    public ResponseEntity restorePicByFilename(String filename, String imgType, Long id) {
        // create correct paths
        String to = imgType + "s/" + imgType + id + "/" + filename ;
        String from = "archive/" + imgType + "s/" + imgType + id + "/" + filename;
        //check if "from" file exists
        if (!hasFile(from)) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(filename + " konnte nicht gefunden werden.");
        //check if "to" file exists
        if (hasFile(to)) return ResponseEntity.status(HttpStatus.CONFLICT).body(filename + " existiert bereits.");
        // then copy the file
        s3Services.copyFile(from, to);

        // load product or user and readd the restored img to picPaths
        if (imgType.equals("product")) {
            Product product = productService.getProductEntityById(id);
            if (product == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("product with Id" + id + " doesn't exist in db.");    // Returns a status = 404 response
            // Then we add the picture to the picPaths
            if (!product.getPicPaths().contains(filename)) {
                ArrayList<String> picPaths = product.getPicPaths();
                picPaths.add(filename);
                product.setPicPaths(picPaths);
                productService.saveOrUpdateProduct(product);
            }
            // delete restored img from archive
            s3Services.deleteFile(from);  // loadImageByFilename() returns a response with the product pic. If the image couldn't be loaded, the response will contain an error message
        }
        // return success entity (OK - 200)
        return ResponseEntity.status(HttpStatus.OK).body(filename + " erfolgreich wiederhergestellt");
    }

    public boolean hasFile(String keyName) {
        ObjectListing objectListing = s3Services.listObjects();
        for(S3ObjectSummary os : objectListing.getObjectSummaries()) {
            if (os.getKey().equals(keyName)) return true;
        }
        return false;
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
        s3Services.deleteFile("products/product" + productId + "/" + filename);
        return ResponseEntity.status(HttpStatus.OK).body(filename + " erfolgreich gelöscht.");  // loadImageByFilename() returns a response with the product pic. If the image couldn't be loaded, the response will contain an error message
    }

    public ResponseEntity deletePicByFilename(String keyName) {
        s3Services.deleteFile(keyName);
        return ResponseEntity.status(HttpStatus.OK).body(keyName + " erfolgreich gelöscht.");  // loadImageByFilename() returns a response with the product pic. If the image couldn't be loaded, the response will contain an error message
    }


    // not tested yet
    public ResponseEntity deleteArchive(String archiveType, String id) {
            if (!hasFile("archive/" + archiveType + "s/" + archiveType + id)) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(archiveType + id + " Archiv konnte nicht gefunden werden.");
            s3Services.deleteFile("archive/" + archiveType + "s/" + archiveType + id);
            if (!hasFile("archive/" + archiveType + "s/" + archiveType + id)) {
                return ResponseEntity.status(HttpStatus.OK).body(archiveType + id + " Archiv erfolgreich gelöscht");
            } else {
                return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body(archiveType + id + " Archiv konnte gefunden, aber nicht gelöscht werden.");
            }
        }

    // not tested yet
    public ResponseEntity deleteImageFolder(String folderType, String id) {
        if (!hasFile(folderType + "s/" + folderType + id)) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Bilder von " + folderType + id + "  konnte nicht gefunden werden.");
        s3Services.deleteFile(folderType + "s/" + folderType + id);
        if (!hasFile(folderType + "s/" + folderType + id)) {
            return ResponseEntity.status(HttpStatus.OK).body("Alle Bilder von " + folderType + id + " erfolgreich gelöscht");
        } else {
            return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body("Bilder von " + folderType + id + " konnte gefunden, aber nicht gelöscht werden.");
        }
    }
}




//
//    public ResponseEntity handleFileUpload(MultipartFile file, Long userId, Long productId, String imgType) {
//        // -> if the image is a userPic -> update the user who posted it with the newly generated photo filePath of the just saved photo
//        if (imgType.equals("userPic")) {
//            return userService.saveUserPic(file, userId);
//        } else if (imgType.equals("productPic")) {
//            // before we store the image, we need to check if the image is already in the archive. If so, we need to delete it there. Otherwise it would be in the archive and in the normal folder at the same time. If you then delete it (transfer it from normal folder to archive, or restore it (transfer it from archive to normal folder) you would get a fileAlreadyExists Exeption.
//            if (checkIfPicIsAlreadyInArchive(file, productId)) deletePicFromArchive(file, productId);
//            Product product = productService.getProductEntityById(productId);                                      // We first load the product, for which we wanna save the pic.
//            return saveProductPic(file, product);
//        }
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Der imgType im Angular Code war falsch...");
//    }
//
//
//
//
//
//    public static void copyFile(String from, String to) throws IOException {
//        Path src = Paths.get(from);
//        Path dest = Paths.get(to);
//        Files.copy(src, dest);
//    }

//
//
//    public ResponseEntity storeImage(MultipartFile file, String type, Path imageFolderLocation) {
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

//    public ResponseEntity getImageByUserId(Long userId) {
//        // The file Names of the ProfilePics are saved in the user entities, so we first need to load the user from the user database table
//        User user = userService.getActiveUser(userId);
//        if (user == null)
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("user with Id" + userId + " doesn't exist in db.");    // Returns a status = 404 response
//        // Then we load the picture from the local storage
//        if (user.getProfilePic() != null) {
//            Resource file = loadUserProfilePic(user.getProfilePic(), user);  // Somehow you can't store the file directly in a variable of type File, instead you need to use a variable of type Resource.
//            System.out.println(file);
//            return ResponseEntity.ok()
//                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")  //the Content-Disposition response header is a header indicating if the content is expected to be displayed inline in the browser, that is, as a Web page or as part of a Web page, or as an attachment, that is downloaded and saved locally.
//                    .body(file);  // the response body now contains the profile pic
//        } else {
//            return ResponseEntity.status(HttpStatus.OK).body("User with userId = " + userId + "has no profilePic yet (user.getProfilePic() = null)");  // Returns a status = 404 response
//        }
//    }

//    public Resource loadUserProfilePic(String filename, User user) {
//        Resource resource = loadImageByFilenameAsResource(filename, "userPic", user.getId());
//        if (resource.exists() || resource.isReadable()) {
//            return resource;
//        } else {
//            throw new RuntimeException("It seems like you deleted the images on the server, without also deleting them in the database ");
//        }
//    }
//
////    /*
////     * returns a response with the product pic. If the image couldn't be loaded, the response will contain an error message
////     */
////    public Resource loadProductPic(String filename, Long productId) {
////        Resource resource = loadImageByFilenameAsResource(filename, "productPic", productId);
////        if (resource.exists() || resource.isReadable()) {
////            return resource;
////        } else {
////            throw new RuntimeException("It seems like you deleted the images on the server, without also deleting them in the database ");
////        }
////    }
//
//
//
//    // This is the function that really loads the image from the local storage
//    public ResponseEntity loadFile(Path filePath) {
//        try {
//            Resource resource = new UrlResource(filePath.toUri());
//            if (resource.exists() || resource.isReadable()) {
//                return ResponseEntity.ok()
//                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"").body(resource);
//            } else {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Foto existiert nicht oder konnte nicht gelesen werden.");
//            }
//        } catch (MalformedURLException ex) {
//            ex.printStackTrace();
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + ex);
//        }
//    }
//
//    public void createFolder(Path FolderLocation) {
//        if (Files.notExists(FolderLocation)) {
//            try {
//                Files.createDirectory(FolderLocation);
//            } catch (IOException e) {
//                throw new RuntimeException("Could not initialize storage!");
//            }
//        }
//    }
//
//    public ResponseEntity deleteImageByFilename(String filename, String type, long id) {
//        Boolean successfullyDeleted;
//        try {
//            Resource resource = loadImageByFilenameAsResource(filename, type, id);
//            successfullyDeleted = new File(resource.getURI()).delete();
//        } catch (IOException e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
//        }
//        return ResponseEntity.status(HttpStatus.OK).body(successfullyDeleted);
//
//    }
//
//    public Boolean checkIfPicIsAlreadyInArchive(MultipartFile file, Long id) {
//        try {
//            loadImageByFilenameAsResource(file.getOriginalFilename(), "archiveProductPic", id);
//            return true;
//        } catch (Exception e) {
////            System.out.println(e);
//            return false;
//        }
//    }
//
//    public ResponseEntity saveProductPic(MultipartFile file, Product product) {
//
//        if (product == null)
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("product with Id" + product.getId() + " doesn't exist in db.");    // Returns a status = 404 response
//        if (!hasAlreadyThisFile(file, product)) {                         // checks, if the Productalready has a Photo with exact same name.
//            ResponseEntity response = storeImageS3(file.getOriginalFilename(), file, "product", product.getId());  //übergibt das Foto zum Speichern an imageService und gibt den Namen des Fotos zum gerade gespeicherten Foto zurück als Response Body. falls Speichern nicht geklappt hat kommt response mit Fehlercode zurück (400 oder ähnliches)
//            if (response.getStatusCodeValue() == 200) {
//                System.out.println("Unter folgendem Namen wurde das Foto lokal (src -> main -> resources -> images) gespeichert: " + file.getOriginalFilename()); // if saving photo was successfull => response status = 200...
//                if (product.getPicPaths() == null) {                                   // we only saved the pic in the pic folder until now, not in the database. A Product can have multiple images, so the pic needs to get stored in a List
//                    ArrayList<String> arr = new ArrayList<>();
//                    //add the new picPath
//                    arr.add(file.getOriginalFilename());
//                    product.setPicPaths(arr);
//                } else {
//                    product.getPicPaths().add(file.getOriginalFilename());
//                    product.setPicPaths(product.getPicPaths());
//                }
//                System.out.println(product.getPicPaths());
//                productService.saveOrUpdateProduct(product);                             //updated das Product in der datenbank, damit der Fotoname da auch gespeichert ist.
//            }
//            return response;
//        }
//        return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body("Foto mit selbem Namen wurde für gleiches Produkt schonmal hochgeladen.");
//    }
//
//    private boolean hasAlreadyThisFile(MultipartFile file, Product product) {
//        if (product.getPicPaths() != null) {
//            Path path = Paths.get(file.getOriginalFilename());
//            System.out.println("Path: " + path);
//            if (product.getPicPaths().contains(path)) return true;
//        }
//        return false;
//    }
//
//
//    // delete img from archive
//    public ResponseEntity deletePicFromArchive(MultipartFile file, Long id) {
//        return deleteImageByFilename(file.getOriginalFilename(), "archiveProductPic", id);  // loadImageByFilename() returns a response with the product pic. If the image couldn't be loaded, the response will contain an error message
//    }
//
//    public Boolean checkContentType(MultipartFile profilePic) {
//        String contentType = profilePic.getContentType();
//        return contentType.equals("image/png") || contentType.equals("image/jpg") || contentType.equals("image/jpeg") || contentType.equals("image/bmp") || contentType.equals("image/gif");
//    }
//}
//
//
////    public List<ResponseEntity<UrlResource>> listFilesForFolder(final Path folderLocation) throws IOException {              // https://stackoverflow.com/questions/1844688/how-to-read-all-files-in-a-folder-from-java
////        try (Stream<Path> paths = Files.walk(Paths.get(String.valueOf(folderLocation)))) {
////            return paths
////                        .filter(Files::isRegularFile)
////                        .map(Path::toUri)
////                        .map(uri -> {
////                            try {
////                                return new UrlResource(uri);
////                            } catch (MalformedURLException e) {
////                                e.printStackTrace();
////                                return null;
////                            }
////                        })
////                        .filter(Resource::isReadable)
////                        .map(urlResource -> ResponseEntity.ok()
////                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; all product pics")  //the Content-Disposition response header is a header indicating if the content is expected to be displayed inline in the browser, that is, as a Web page or as part of a Web page, or as an attachment, that is downloaded and saved locally.
////                            .body(urlResource) )
////                        .collect(Collectors.toList());
////        }
////    }

