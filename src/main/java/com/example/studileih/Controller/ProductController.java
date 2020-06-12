package com.example.studileih.Controller;

import com.example.studileih.Dto.ProductDto;
import com.example.studileih.Entity.Product;
import com.example.studileih.Entity.User;
import com.example.studileih.Service.ImageService;
import com.example.studileih.Service.ProductService;
import com.example.studileih.Service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@CrossOrigin
public class ProductController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private ModelMapper modelMapper;

    @PostConstruct
    public void createBaseDataset() {
        Product monopoly = new Product("Monopoly");
        monopoly.setCreatedAt(Calendar.getInstance().getTime());
        monopoly.setUpdatedAt(Calendar.getInstance().getTime());
        productService.saveOrUpdateProduct((Product) monopoly);
    }
    /**
     * @return: all products from the repository
     */
    @GetMapping("allProducts")
    public List<ProductDto> getAllProducts() {
        List<Product> allProducts = new ArrayList<>();
        allProducts = productService.listAllProducts();
        System.out.println(allProducts.get(0).toString());
        return allProducts.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private ProductDto convertToDto(Product product) {
        ProductDto productDto = modelMapper.map(product, ProductDto.class);
        productDto.setCreatedAt(product.getCreatedAt(),
                ZonedDateTime.now(ZoneId.systemDefault()).toString()); //irgendwann mal durch die Zeitzone des Nutzers ersetzen.
        productDto.setUpdatedAt(product.getUpdatedAt(),
                ZonedDateTime.now(ZoneId.systemDefault()).toString());
        return productDto;
    }

    /*
     * method for posting images into database from angular
     */
    List<String> files = new ArrayList<>();
    String filePath;
    @PostMapping("/postImage")
    public void handleFileUpload(@RequestParam("file") MultipartFile file, String userId, String groupId, String postId, String imgType) {
        // Update User Long.parseLong(userId) or group or post depending on imgType:
        // -> if the image is a userPic -> update user with the newly generated photoId of the just saved photo
        if (imgType.equals("userPic")) {
            Optional<User> optionalEntity =  userService.getUserById(Long.parseLong(userId));
            User user = optionalEntity.get();
            try {
                imageService.store(file);
                filePath = file.getOriginalFilename();
                files.add(filePath);
            } catch (Exception e) {
                System.out.println("Error at groupController.handleFileUpload():" + e);
            }
            user.setProfilePic(filePath);
            userService.saveOrUpdateUser(user);

        } else if (imgType.equals("productPic")) {
            //TODO:
            System.out.println("groupController.postImage-> groupPics are not programmed yet");
        } else if (imgType.equals("postPic")) {
            //TODO
            System.out.println("groupController.postImage-> postPics are not programmed yet");
        }
    }

    /*
     * getting all images from local storage! to angular
     */
    @GetMapping("/allImages")
    public ResponseEntity<List<String>> getListFiles(Model model) {
        List<String> fileNames = files
                .stream().map(fileName -> MvcUriComponentsBuilder
                        .fromMethodName(ProductController.class, "getFile", fileName).build().toString())
                .collect(Collectors.toList());

        return ResponseEntity.ok().body(fileNames);
    }


}