package com.example.studileih.Controller;

import com.example.studileih.Dto.ProductDto;
import com.example.studileih.Entity.Product;
import com.example.studileih.Entity.User;
import com.example.studileih.Service.ImageService;

import com.example.studileih.Service.ProductBuilder;
import com.example.studileih.Service.ProductService;
import com.example.studileih.Service.UserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiResponse;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import javax.annotation.PostConstruct;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_OK;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@CrossOrigin
@Api(tags = "Products API - controller methods for managing Products")
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
     * Converts a Product to a ProductDto. The createdAt and updatedAt Dates are converted to simple Strings, because Date is Java specific and can't be send to Angular.
     *
     * @param product
     * @return productDto
     */
    private ProductDto convertToDto(Product product) {
        ProductDto productDto = modelMapper.map(product, ProductDto.class);
        // falls ein Product ein CreatedAt oder UpdatedAt Value hat, muss der zu einem String konvertiert werden. Falls nicht darf das Date nicht konvertiert werden, sonst gibts NullPointerExceptions.
        if (product.getCreatedAt() != null)
            productDto.setCreatedAt(product.getCreatedAt(), ZonedDateTime.now(ZoneId.systemDefault()).toString()); //konvertiert Date zu String -> einfachhaltshaber nimmts immer die Zeitzone des Servers (ZoneId.systemdefault), vielleicht irgendwann mal durch die Zeitzone des Nutzers ersetzen.
        if (product.getUpdatedAt() != null)
            productDto.setUpdatedAt(product.getUpdatedAt(), ZonedDateTime.now(ZoneId.systemDefault()).toString());
        return productDto;
    }

    /**
     * @return: all products from the repository
     */
    @GetMapping("products")
    @ApiOperation(value = "Return all available products converted to DTOs")
    public List<ProductDto> getAllProducts() {
        List<Product> allProducts = productService.listAllProducts();
        //System.out.println(allProducts.get(0).toString());
        return allProducts.stream()                 // List<Product> muss zu List<ProductDto> konvertiert werden. Hier tun wir zuerst die List<Product> in einen Stream umwandeln
                .map(this::convertToDto)            // Dann jedes Product ausm Stream herausnehmen und zu ProductDto umwandeln
                .collect(Collectors.toList());      // und dann den neuen Stream als List<ProductDto> einsammeln.
    }

    @GetMapping("/products/{id}")
    @ApiOperation(value = "Returns a product entity by its ID. The result is not clean enough, be careful")

    public ProductDto getProduct(@PathVariable Long id) {
        Optional<Product> optional = productService.getProductById(id);   // the id always comes as a string from angular, even when you send it as a number in angular...
        Product product = optional.get();
        return convertToDto(product);
    }

    @GetMapping("/productsdto/{id}")
    @ApiOperation(value = "Return one product by ID as DTO, in order to avoid Entity-related issues")
    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "Everything OK"),
            @ApiResponse(code = SC_BAD_REQUEST, message = "An unexpected error occurred")
    })
    public List<ProductDto> getProductDto(@PathVariable Long id) {
        return productService.getProductDtoById(id);
    }

    @PostMapping(path = "/products")
    @ApiOperation(value = "Add a new product to the database")
    public ResponseEntity<String> addProduct(Long id,
                                             String description,
                                             String title,
                                             String category,
                                             Long userId,
                                             double price,
                                             boolean isBeerOk,
                                             String startDate,
                                             String endDate,
                                             String pickUpTime,
                                             String returnTime,
                                             MultipartFile[] imageFiles) {

        System.out.println("---------------------");
        System.out.println(imageFiles);
        System.out.println(imageFiles.length);
//        System.out.println(imageFiles[0]);
//        System.out.println(imageFiles[imageFiles.length-1]);
        System.out.println("--------------");

        System.out.println(startDate);

        Date startDay = productService.transformStringToDate(startDate);
        Date endDay = productService.transformStringToDate(endDate);

        System.out.println(startDay);
        System.out.println(endDay);

        // first we get the user who added the product
        User productOwner = userService.getUserById(userId).get();
        System.out.println(imageFiles);

        // then we create the product
        Product product = new ProductBuilder().withTitle(title).withDescription(description).withCategory(category)
                .withPrice(price).withIsBeerOk(isBeerOk).withStartDay(startDay).withEndDay(endDay).withUser(productOwner)
                .withPickUpTime(pickUpTime).withReturnTime(returnTime).withAvailable(true).withDorm(productOwner.getDorm().getName()).withCity(productOwner.getDorm().getCity()).build();


        // save the product
        productService.saveOrUpdateProduct(product);
        // if there were product pics uploaded, we also save them
        Arrays.asList(imageFiles)
                .stream()
                .forEach(file -> imageService.saveProductPic(file, product));
        return ResponseEntity.status(HttpStatus.OK).body("Produkt erfolgreich angelegt.");
    }

    @PutMapping(path = "/products")
    @ApiOperation(value = "Edit an existing product")
    public ResponseEntity<String> editProduct(Long id,
                                             String description,
                                             String title,
                                             String category,
                                             Long userId,
                                             double price,
                                             boolean isBeerOk,
                                             String startDate,
                                             String endDate,
                                             String pickUpTime,
                                             String returnTime,
                                             MultipartFile[] imageFiles) {

        System.out.println("---------------------");
        System.out.println(imageFiles);
        System.out.println(imageFiles.length);
        System.out.println(imageFiles[0]);
        System.out.println(imageFiles[imageFiles.length-1]);
        System.out.println("--------------");

      Date startDay = productService.transformStringToDate(startDate);
      Date endDay = productService.transformStringToDate(endDate);

        System.out.println(startDay);
        System.out.println(endDay);

        // if an id was provided this means we want to update the product, not make a new one.
        Product product = null;
        if (id != null) {
            product = productService.getProductById(id).get();
            product.setTitle(title);
            product.setDescription(description);
            if (category != null) {
                product.setCategory(category);
            }
            product.setPrice(price);
            product.setBeerOk(isBeerOk);
            product.setStartDay(startDay);
            product.setEndDay(endDay);
            product.setPickUpTime(pickUpTime);
            product.setReturnTime(returnTime);
            product.setAvailable(true);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Zum Editieren muss eine Produkt Id angegben werden.");
        }

        // save the product
        productService.saveOrUpdateProduct(product);
        // if there were product pics uploaded, we also save them
        Product finalProduct = product;   // variable in Lambda Expression should be final
        Arrays.asList(imageFiles)
                .stream()
                .forEach(file -> imageService.saveProductPic(file, finalProduct));
        return ResponseEntity.status(HttpStatus.OK).body("Produkt erfolgreich editiert.");
    }




    @PostMapping(value = "/products/delete/{id}")
    @ApiOperation(value = "Deletes one product identified by its ID")
    public ResponseEntity<String> deleteProduct(Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.status(HttpStatus.OK).body("Produkt erfolgreich gelöscht.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Produkt existiert nicht mehr in der Datenbank.");
        }
    }

    @PutMapping(value = "/products/{id}")
    @ApiOperation(value = "Updates one product identified by its ID.")
    public void updateProduct(Product product, @PathVariable Long id) {
        productService.updateProduct(product, id);
    }


}