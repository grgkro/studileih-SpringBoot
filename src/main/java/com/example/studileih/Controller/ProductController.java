package com.example.studileih.Controller;

import com.example.studileih.Dto.ProductDto;
import com.example.studileih.Service.DormService;
import com.example.studileih.Service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@CrossOrigin
@Api(tags = "Products API - controller methods for managing Products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private DormService dormService;

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
    @GetMapping("/productsByDorm/{id}")
    @ApiOperation(value = "Return all available products of one dorm converted to DTOs")
    public List<ProductDto> getProductsByDorm(@PathVariable Long id) {
        return productService.getProductsByDorm(dormService.getDormById(id).get().getName());
    }

    /**
     * @return: all products except the ones from one dormitory from the repository
     */
    @GetMapping("/productsWithouthDormProducts/{id}")
    @ApiOperation(value = "Return all the other available products that are not from that dorm")
    public List<ProductDto> getProductsWithouthDormProducts(@PathVariable Long id) {
        List<ProductDto> allProducts = productService.listAllProducts();
        List<ProductDto> productsFromDorm = productService.getProductsByDorm(dormService.getDormById(id).get().getName());
        return allProducts.stream().filter(productDto -> !productsFromDorm.contains(productDto)).collect(Collectors.toList());
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
    public ResponseEntity addProduct(String description,
                                             String title,
                                             String category,
                                             Principal userDetails,
                                             double price,
                                             boolean isBeerOk,
                                             String startDate,
                                             String endDate,
                                             String pickUpTime,
                                             String returnTime,
                                             MultipartFile[] imageFiles) {

        ResponseEntity response = productService.validateInput(null, description, title, category, userDetails, price, startDate, endDate, pickUpTime, returnTime, imageFiles);
        if (response.getStatusCodeValue() != 200) {
            return response;
        }
        return productService.addProduct(description, title, category, userDetails, price, isBeerOk, startDate, endDate, pickUpTime, returnTime, imageFiles);
    }

    @PutMapping(path = "/products")
    @ApiOperation(value = "Edit an existing product")
    public ResponseEntity editProduct(Long id,
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
        ResponseEntity response = productService.validateInput(id, description, title, category, null, price, startDate, endDate, pickUpTime, returnTime, imageFiles);
        if (response.getStatusCodeValue() != 200) {
            return response;
        }
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
}