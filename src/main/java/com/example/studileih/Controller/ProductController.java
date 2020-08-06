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
    public ResponseEntity<String> addProduct(String description,
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
        System.out.println(startDate);
        //transform LocalDate to date: https://beginnersbook.com/2017/10/java-convert-localdate-to-date/
        // 1. get default time zone TODO: Get the actual user time zone from angular!
        ZoneId defaultZoneId = ZoneId.systemDefault();
        LocalDate localStartDay = null;
        Date startDay = null;
        Date endDay = null;
        if (startDate != null) {
            int positionDot = startDate.indexOf(".");
            if (positionDot != -1) {
                 startDate = startDate.substring(0, positionDot );  // if the user didnt change the default start date, the startdate will come in format 2123123123.23 or 2123123123.232 contain milliseconds cuts of the milliseconds (we only need seconds)
            }
            Instant instantStart = Instant.ofEpochSecond(Long.parseLong(startDate));
            localStartDay = instantStart.atZone(ZoneId.systemDefault()).toLocalDate();   // we only need the Day of the year, not the hours + minutes -> localDate() instead of LocalDateTime()
            //2. local date + atStartOfDay() + default time zone + toInstant() = Date
            startDay = Date.from(localStartDay.atStartOfDay(defaultZoneId).toInstant());
        }
        System.out.println(endDate);
        LocalDate localEndDay = null;
        if (endDate != null) {
            Instant instantEnd = Instant.ofEpochSecond(Long.parseLong(endDate));
            localEndDay = instantEnd.atZone(ZoneId.systemDefault()).toLocalDate();
            //2. local date + atStartOfDay() + default time zone + toInstant() = Date
            endDay = Date.from(localEndDay.atStartOfDay(defaultZoneId).toInstant());
        }

        System.out.println(startDay);
        System.out.println(endDay);

        // first we get the user who added the product
        User productOwner = userService.getUserById(userId).get();
        System.out.println(imageFiles);

        // then we create the product
        Product product = new ProductBuilder().withTitle(title).withDescription(description).withCategory(category)
                .withPrice(price).withIsBeerOk(isBeerOk).withStartDay(startDay).withEndDay(endDay).withUser(productOwner)
                .withPickUpTime(pickUpTime).withReturnTime(returnTime).withAvailable(true).build();
        System.out.println(category);
        if (category != null) {
            product.setCategory(category);
        }
        // save the product
        productService.addProduct(product);
        // if there were product pics uploaded, we also save them
        Arrays.asList(imageFiles)
                .stream()
                .forEach(file -> imageService.saveProductPic(file, product.getId()));
        return ResponseEntity.status(HttpStatus.OK).body("Produkt erfolgreich angelegt.");
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