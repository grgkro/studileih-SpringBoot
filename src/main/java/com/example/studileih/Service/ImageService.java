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

    Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private final String basePath = new File("").getAbsolutePath();
    private final Path imageFolderLocation = Paths.get(basePath + "/src/main/resources/images");

    public void store(MultipartFile file) {
        try {
            Files.copy(file.getInputStream(), this.imageFolderLocation.resolve(file.getOriginalFilename()));
        } catch (Exception e) {
            throw new RuntimeException("storageService.store(): FAIL! " + this.imageFolderLocation + " -- File path wrong or photo already exists. (don't upload the same picture twice)");
        }
    }

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

    public void deleteAll() {
        FileSystemUtils.deleteRecursively(imageFolderLocation.toFile());
    }

    public void init() {
        try {
            Files.createDirectory(imageFolderLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage!");
        }
    }
}

    // Creates and saves two hard coded image for programming and testing purposes
    //	@PostConstruct
    //	public void initialEntries () {
    //		// load image 1 from resources folder
    //	    ClassPathResource blackImgFile = new ClassPathResource("image/jsa_about_img_black_background.png");
    //	    byte[] arrayBlackPic = null;
    //		try {
    //			arrayBlackPic = new byte[(int) blackImgFile.contentLength()];
    //		} catch (IOException e1) {
    //			// TODO Auto-generated catch block
    //			e1.printStackTrace();
    //		}
    //	    try {
    //			blackImgFile.getInputStream().read(arrayBlackPic);
    //		} catch (IOException e) {
    //			// TODO Auto-generated catch block
    //			e.printStackTrace();
    //		}
    //
    //	    // load image 2 from resources folder
    //	    ClassPathResource blueImgFile = new ClassPathResource("image/jsa_about_img_blue_background.png");
    //	    byte[] arrayBluePic = null;
    //		try {
    //			arrayBluePic = new byte[(int) blueImgFile.contentLength()];
    //		} catch (IOException e1) {
    //			// TODO Auto-generated catch block
    //			e1.printStackTrace();
    //		}
    //	    try {
    //	    	blueImgFile.getInputStream().read(arrayBluePic);
    //		} catch (IOException e) {
    //			// TODO Auto-generated catch block
    //			e.printStackTrace();
    //		}
    //
    //	    //create the two images
    //	    Image blackImage = new Image(arrayBlackPic, "JSA-ABOUT-IMAGE-BLACK-BACKGROUND", "png", LocalDateTime.now());
    //	    Image blueImage = new Image(arrayBluePic, "JSA-ABOUT-IMAGE-BLUE-BACKGROUND", "png", LocalDateTime.now());
    //
    //	    // store images to HeidiSQL via SpringJPA
    //	    imageRepository.save(blackImage);
    //	    imageRepository.save(blueImage);
    //
    //	}


/*
    // find and return the Base64Code of the profile photo that belongs to the userId
    public String getImageByUserId(Long userId) {
        // 1. retrieve the photoId of the profile pic
        User user = userService.readUserById(userId);
        Long photoId = user.getPhotoId();
        // 2. find all images and take the Base64Code of the photo with the matching photoId
        List<Image> allImages = findImages();
        String base64Code = null;
        for (Image image : allImages) {
            if (image.getId() == photoId) {
                base64Code = image.getBase64code();
            }
        }
        return base64Code;
    }

    /**
     * returns list of string which are images in base 64 encoded
     *
     * @param array of mulitpart files which is retrived from controller
     * @return ListcString>

    public String generateBase64(MultipartFile file) {
        String image = null;
        try {
            image = new String(Base64.getEncoder().encode(file.getBytes()));
        } catch (IOException e) {
            System.out.println(e.getStackTrace());
        }
        return image;
    }
*/

