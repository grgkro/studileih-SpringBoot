package com.example.studileih.Controller;

import com.example.studileih.Service.ImageService;
import com.example.studileih.Service.S3ImageArchiveAndDeleteService;
import com.example.studileih.Service.S3ImageService;
import com.example.studileih.Service.S3Services;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@CrossOrigin
@Api(tags = "Images API - controller methods for managing Images")
public class ImageController {

    @Autowired
    private ImageService imageService;

    @Autowired
    private S3ImageService s3imageService;

    @Autowired
    private S3ImageArchiveAndDeleteService s3ImageArchiveAndDeleteService;

    @Autowired
    S3Services s3Services;

    @PostMapping("/api/file/upload")
    public String uploadMultipartFile(@RequestParam("keyname") String keyName, @RequestParam("uploadfile") MultipartFile file) {
        s3Services.uploadFile(keyName, file);
        return "Upload Successfully. -> KeyName = " + keyName;
    }

    /*
     * Download Files
     */
    @GetMapping("/api/file/{keyname}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String keyname) {
        ByteArrayOutputStream downloadInputStream = s3Services.downloadFile(keyname);

        return ResponseEntity.ok()
                .contentType(contentType(keyname))
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"" + keyname + "\"")
                .body(downloadInputStream.toByteArray());
    }

    private MediaType contentType(String keyname) {
        String[] arr = keyname.split("\\.");
        String type = arr[arr.length-1];
        switch(type) {
            case "txt": return MediaType.TEXT_PLAIN;
            case "png": return MediaType.IMAGE_PNG;
            case "jpg": return MediaType.IMAGE_JPEG;
            default: return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    /*
     * loads a product pic
     */
    @PostMapping("/images/loadProductPicByFilename")    //https://bezkoder.com/spring-boot-upload-multiple-files/
    @ApiOperation(value = "Loads and returns the image of a product by it's provided filename")
    public ResponseEntity loadProductPicByFilename(String filename, Long productId) {
      return s3imageService.loadProductPicByFilenameS3( filename, productId);
//      return imageService.loadProductPicByFilename( filename, productId);
    }

    @PostMapping("/postImage")
    @ApiOperation(value = "Add new Image")
    public ResponseEntity handleFileUpload (MultipartFile file, Long userId, Long productId, String imgType){
       return imageService.handleFileUpload(file, userId, productId, imgType);
    }


    /*
     * archiviert ein user oder product pic, falls sich der Nutzer nach Löschen auf "Rückgängig" klickt.
     * Ob das imag als user oder product pic abgespeichert wird, hängt vom imgType ab ("product" oder "user")
     */
    @PostMapping("/images/archivePicByFilename")
    @ApiOperation(value = "Transfers an deleted image from the user or product folder to the archive folder, so that it can be restored later.")
    public ResponseEntity archiveProductPicByFilename( String filename, String imgType, String productId) throws IOException {
        return s3ImageArchiveAndDeleteService.archivePicByFilename(filename, imgType, productId);
    }

    /*
     * gets an archived product pic from the archive and restores it to the local storage and the database
     */
    @PostMapping("/images/restorePicByFilename")
    @ApiOperation(value = "Restores a deleted image from the archive to the user or product folder")
    public ResponseEntity restorePicByFilename(String filename, String imgType, Long productId) throws IOException {
       return imageService.restorePicByFilename(filename, imgType, productId);
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
           return s3ImageArchiveAndDeleteService.deleteProductPicByFilename (filename, productId);
        }

    @PostMapping("/loadProfilePicByUserId")
    @ApiOperation(value = "Add new Profile Image to User identified by ID")
    public ResponseEntity getImageByUserId (@RequestBody Long userId){
          return s3imageService.getImageByUserId(userId);
     }
    }