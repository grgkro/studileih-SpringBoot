package com.example.studileih.Entity;

import com.example.studileih.Entity.BaseEntity;
import com.example.studileih.Entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.nio.file.Path;
import java.util.ArrayList;

@Entity
@Data
@NoArgsConstructor
public class Product extends BaseEntity {

     private String name;
     private String title;
     private String type;
     private double price = 0;
     private int views = 0; //How often was the product viewed?
     private boolean available = true; //is it available?
     private ArrayList<Path> picPaths;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;

    //for testing
    public Product(String name) {
        this.name = name;
    }
}