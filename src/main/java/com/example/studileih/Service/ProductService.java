package com.example.studileih.Service;

import com.example.studileih.Dto.ProductDto;
import com.example.studileih.Entity.Product;
import com.example.studileih.Repository.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public List<Product> listAllProducts() {
        List<Product> products = new ArrayList<>();
        productRepository.findAll().forEach(products::add);  // products::add ist gleich wie: products.add(product)
        return products;
    }

    public boolean addProduct(Product product) {
        productRepository.save(product);
        return true;
    }


    public Product updateProduct(Product newProduct, Long id) {
        Product oldProduct = getProductById(id).get();

        if(oldProduct.isAvailable()){
            oldProduct.setName(newProduct.getName());
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