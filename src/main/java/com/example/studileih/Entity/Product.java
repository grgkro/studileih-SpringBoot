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
//@Data // Irgendwie funktioniert die @Data Annotation bei mir (Georg) nicht mehr richtig, und auch nur bei der Product Entity hier... deshalb musste ich die Getter & Setter hier wieder von Hand machen. Sobald ich das Problem gelöst habe, lösche ich sie wieder.
@NoArgsConstructor
public class Product extends BaseEntity {

     private String name;
     private String title;
     private String type;
     private double price = 0;
     private int views = 0; //How often was the product viewed?
     private boolean available = true; //is it available?
     private ArrayList<Path> picPaths;

    @ManyToOne()
    @JoinColumn(name = "user_id")
    private User user;

    //um schnell ein Produkt zum Testen erzeugen zu können:
    public Product(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public ArrayList<Path> getPicPaths() {
        return picPaths;
    }

    public void setPicPaths(ArrayList<Path> picPaths) {
        this.picPaths = picPaths;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}