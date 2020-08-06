package com.example.studileih.Service;

import com.example.studileih.Dto.ProductDto;

import com.example.studileih.Dto.UserDto;
import com.example.studileih.Entity.Product;
import com.example.studileih.Entity.User;
import com.example.studileih.Repository.ProductRepository;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
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

    public Product addProduct(Product product) {
        Product product2 = productRepository.save(product);
        return product2;
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