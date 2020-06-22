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
    private ModelMapper modelMapper;  //modelMapper konvertiert Entities in DTOs (modelMapper Dependency muss in pom.xml drin sein)

    /**
     * Die Funktion wird direkt nach Start aufgerufen und speichert 1 Beispielprodukt in die DB -> Kann später auskommentiert/gelöscht werden
      */
    @PostConstruct
    public void createBaseDataset() {
        Product monopoly = new Product("Monopoly");
        // Ich hab CreatedAt und UpdatedAt jetzt so eingestellt, dass es keine Pflichtfelder mehr sind, das ist einfacher am Anfang. Sollten später wieder zu Pflichtfeldern gemacht werden, dazu muss in BaseEntity das nullable = false wieder hinzugefügt werden
        // monopoly.setCreatedAt(Calendar.getInstance().getTime());
        // monopoly.setUpdatedAt(Calendar.getInstance().getTime());
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
        return allProducts.stream()                 // List<Product> muss zu List<ProductDto> konvertiert werden. Hier tun wir zuerst die List<Product> in einen Stream umwandeln
                .map(this::convertToDto)            // Dann jedes Product ausm Stream herausnehmen und zu ProductDto umwandeln
                .collect(Collectors.toList());      // und dann den neuen Stream als List<ProductDto> einsammeln.
    }

    /**
     * Converts a Product to a ProductDto. The createdAt and updatedAt Dates are converted to simple Strings, because Date is Java specific and can't be send to Angular.
     * @param product
     * @return productDto
     */
    private ProductDto convertToDto(Product product) {
        ProductDto productDto = modelMapper.map(product, ProductDto.class);
        // falls ein Product ein CreatedAt oder UpdatedAt Value hat, muss der zu einem String konvertiert werden. Falls nicht darf das Date nicht konvertiert werden, sonst gibts NullPointerExceptions.
        if (product.getCreatedAt() != null) productDto.setCreatedAt(product.getCreatedAt(), ZonedDateTime.now(ZoneId.systemDefault()).toString()); //konvertiert Date zu String -> einfachhaltshaber nimmts immer die Zeitzone des Servers (ZoneId.systemdefault), vielleicht irgendwann mal durch die Zeitzone des Nutzers ersetzen.
        if (product.getUpdatedAt() != null) productDto.setUpdatedAt(product.getUpdatedAt(), ZonedDateTime.now(ZoneId.systemDefault()).toString());
        return productDto;
    }

    /*
     * method for posting images into the image folder (src -> main -> resources -> images) and put the filePath into the database.
     */
    @PostMapping("/postImage")
    public void handleFileUpload(@RequestParam("file") MultipartFile file, String userId, String groupId, String postId, String imgType) {
        // -> if the image is a userPic -> update the user who posted it with the newly generated photo filePath of the just saved photo
        if (imgType.equals("userPic")) {
            Optional<User> optionalEntity =  userService.getUserById(Long.parseLong(userId));
            User user = optionalEntity.get();
            String imageName = null;
            try {
                imageName = imageService.store(file); //übergibt das Photo zum Speichern an imageService und gibt den Namen des Photos zum gerade gespeicherten Photo zurück
                System.out.println("Unter folgendem Namen wurde das Foto lokal (src -> main -> resources -> images) gespeichert: " + imageName);
            } catch (Exception e) {
                System.out.println("Error at groupController.handleFileUpload():" + e);
            }
            user.setProfilePic(imageName);  //verknüpft den Photonamen mit dem User, der es hochgeladen hat
            userService.saveOrUpdateUser(user); //updated den User in der datenbank, damit der Photoname da auch gespeichert ist.
        } else if (imgType.equals("productPic")) {
            //TODO: Product Fotos auch speicherbar machen (bisher gehen nur Userfotos)
            System.out.println("groupController.postImage-> groupPics are not programmed yet");
        } else if (imgType.equals("postPic")) {
            //TODO:
            //ich weiß grad auch nichtmehr, was ein postPic sein sollte... Vielleicht wenn etwas Offtopic geposted wird?
            System.out.println("groupController.postImage-> postPics are not programmed yet");
        }
    }

}