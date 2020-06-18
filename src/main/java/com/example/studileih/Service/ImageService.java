package com.example.studileih.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ImageService {

    // Logger is similar to system.out.println, but you can also see the outprint on a server-log (was useful for running on AWS Cloud)
    Logger log = LoggerFactory.getLogger(this.getClass().getName());

    // basePath is the root folder where you saved the project. imageFolderLocation completes the basePath to the image folder location
    private final String basePath = new File("").getAbsolutePath();
    private final Path imageFolderLocation = Paths.get(basePath + "/src/main/resources/images");

    /**
     * The image gets send from the frontend as a Multipartfile. Here we save it.
     * There can't be two images in the image folder with the same name, so we throw an exception if that happens.
     * TODO: Find a better solution here, at least tell the user at the frontend that the upload didn't work.
     * @param file
     */
    public String store(MultipartFile file) {
        try {
            Files.copy(file.getInputStream(), this.imageFolderLocation.resolve(file.getOriginalFilename()));
        } catch (Exception e) {
            throw new RuntimeException("storageService.store(): FAIL! " + this.imageFolderLocation + " -- File path wrong or photo already exists. (don't upload the same picture twice)");
        }
        return file.getOriginalFilename();
    }

    // wird noch nicht benutzt
    public Resource loadFile(String filename) {
        try {
            Path file = imageFolderLocation.resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("FAIL!");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("FAIL!");
        }
    }

    // wird noch nicht benutzt
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(imageFolderLocation.toFile());
    }

    // wird noch nicht benutzt
    public void init() {
        try {
            Files.createDirectory(imageFolderLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage!");
        }
    }
}



