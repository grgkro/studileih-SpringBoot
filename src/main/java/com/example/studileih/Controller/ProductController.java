package com.example.studileih.Controller;

import com.example.studileih.Dto.ProductDto;
import com.example.studileih.Entity.Dorm;
import com.example.studileih.Entity.Product;
import com.example.studileih.Entity.User;
import com.example.studileih.Service.*;

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
    private DormService dormService;

    @Autowired
    private ModelMapper modelMapper;  //modelMapper konvertiert Entities in DTOs (modelMapper Dependency muss in pom.xml drin sein)



    /**
     * @return: all products from the repository
     */
    @GetMapping("/products")
    @ApiOperation(value = "Return all available products converted to DTOs")
    public List<ProductDto> getAllProducts() {
        List<ProductDto> allProducts = productService.listAllProducts();
        //System.out.println(allProducts.get(0).toString());
        return allProducts;
    }

    /**
     * @return: all products of one dormitory from the repository
     */
    @GetMapping("/productsByDorm")
    @ApiOperation(value = "Return all available products of one dorm converted to DTOs")
    public List<ProductDto> getProductsByDorm(@PathVariable Long id) {
        return productService.getProductsByDorm(dormService.getDormById(id).get().getName());
    }

    @GetMapping("/products/{id}")
    @ApiOperation(value = "Returns a product entity by its ID. The result is not clean enough, be careful")
    public ProductDto getProduct(@PathVariable Long id) {
        return productService.getProductById(id);
    }

//    @GetMapping("/productsdto/{id}")
//    @ApiOperation(value = "Return one product by ID as DTO, in order to avoid Entity-related issues")
//    @ApiResponses(value = {@ApiResponse(code = SC_OK, message = "Everything OK"),
//            @ApiResponse(code = SC_BAD_REQUEST, message = "An unexpected error occurred")
//    })
//    public List<ProductDto> getProductDto(@PathVariable Long id) {
//        return productService.getProductById(id);
//    }

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

        return productService.addProduct(id, description, title, category, userId, price, isBeerOk, startDate, endDate, pickUpTime, returnTime, imageFiles);
    }

    @PutMapping(path = "/products")
    @ApiOperation(value = "Edit an existing product")
    public ResponseEntity<String> editProduct(Long id,
                                             String description,
                                             String title,
                                             String category,
                                             double price,
                                             boolean isBeerOk,
                                             String startDate,
                                             String endDate,
                                             String pickUpTime,
                                             String returnTime,
                                             MultipartFile[] imageFiles) {

      return productService.editProduct(id, description, title, category, price, isBeerOk, startDate, endDate, pickUpTime, returnTime, imageFiles);
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

//    @PutMapping(value = "/products/{id}")
//    @ApiOperation(value = "Updates one product identified by its ID.")
//    public void updateProduct(Product product, @PathVariable Long id) {
//        productService.updateProduct(product, id);
//    }


}