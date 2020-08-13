package com.example.studileih.Service;

import com.example.studileih.Dto.ProductDto;

import com.example.studileih.Dto.UserDto;
import com.example.studileih.Entity.Product;
import com.example.studileih.Entity.User;
import com.example.studileih.Repository.ProductRepository;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ModelMapper modelMapper;

    public void saveOrUpdateProduct(Product product) {
        productRepository.save(product);
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }
    
    public List<ProductDto> getProductDtoById(Long id) {
    	//leere Liste für das Ergebnis
    	List<ProductDto> productDto = new ArrayList<>();
    	//Umwandlung: gefundene Produkt zu Liste
    	List<Product> products= productRepository.findById(id).stream().collect(Collectors.toList());
    	//Umwandlung: users zu Dtos
    	for (Product produkt : products) {
    		ProductDto produktDto = modelMapper.map(produkt, ProductDto.class);
    		productDto.add(produktDto);
    	}
    	return productDto;
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public List<Product> listAllProducts() {
        List<Product> products = new ArrayList<>();
        productRepository.findAll().forEach(products::add);  // products::add ist gleich wie: products.add(product)
        return products;
    }

    public Date transformStringToDate(String stringDate) {
        //transform LocalDate to date: https://beginnersbook.com/2017/10/java-convert-localdate-to-date/
        // 1. get default time zone TODO: Get the actual user time zone from angular!
        ZoneId defaultZoneId = ZoneId.systemDefault();
        Date date = null;
        LocalDate localStartDay = null;
        if (stringDate != null) {
            int positionDot = stringDate.indexOf(".");
            if (positionDot != -1) {
                stringDate = stringDate.substring(0, positionDot );  // if the user didnt change the default start date, the startdate will come in format 2123123123.23 or 2123123123.232 contain milliseconds cuts of the milliseconds (we only need seconds)
            }
            Instant instantStart = Instant.ofEpochSecond(Long.parseLong(stringDate));
            localStartDay = instantStart.atZone(ZoneId.systemDefault()).toLocalDate();   // we only need the Day of the year, not the hours + minutes -> localDate() instead of LocalDateTime()
            //2. local date + atStartOfDay() + default time zone + toInstant() = Date
            date = Date.from(localStartDay.atStartOfDay(defaultZoneId).toInstant());
        }
        return date;
    }

    public Product updateProduct(Product newProduct, Long id) {
        Product oldProduct = getProductById(id).get();

        if(oldProduct.isAvailable()){
            oldProduct.setDescription(newProduct.getDescription());
            oldProduct.setTitle(newProduct.getTitle());
            oldProduct.setType(newProduct.getType());
            oldProduct.setPrice(newProduct.getPrice());
            oldProduct.setViews(newProduct.getViews());
            oldProduct.setAvailable(newProduct.isAvailable());
            oldProduct.setPicPaths(newProduct.getPicPaths());

            productRepository.save(oldProduct);
            System.out.println(oldProduct);
            return oldProduct;
        }

        return null;
    }

}