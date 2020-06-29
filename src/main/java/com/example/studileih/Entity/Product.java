package com.example.studileih.Entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;

@Entity
@Data // Irgendwie funktioniert die @Data Annotation bei mir (Georg) nicht mehr richtig, und auch nur bei der Product Entity hier... deshalb musste ich die Getter & Setter hier wieder von Hand machen. Sobald ich das Problem gelöst habe, lösche ich sie wieder.
@NoArgsConstructor
public class Product {
    @Id  // for more details why the ID is generated like this: https://stackoverflow.com/questions/62548985/hibernate-generates-one-id-for-two-tables-entities/62549190?noredirect=1#comment110616128_62549190
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_generator")
    @SequenceGenerator(name="product_generator", sequenceName = "product_seq", allocationSize=1)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", updatable = false)
    //  @Column(name = "created_at", nullable = false, updatable = false)  // durch nullable = false wird es zu Pflichtfeld
    @CreatedDate
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    //  @Column(name = "updated_at", nullable = false) // durch nullable = false wird es zu Pflichtfeld
    @LastModifiedDate
    private Date updatedAt;

     private String name;
     private String title;
     private String type;
     private double price = 0;
     private int views = 0; //How often was the product viewed?
     private boolean available = true; //is it available?
     private ArrayList<String> picPaths;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    //um schnell ein Produkt zum Testen erzeugen zu können:
    public Product(String name) {
        this.name = name;
    }
    //auch zum Testen
    public Product(String name, String title, double price) {
        this.name = name;
        this.title = title;
        this.price = price;
    }

    public Product(String name, String title, double price, User user) {
        this.name = name;
        this.title = title;
        this.price = price;
        this.user = user;
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

    public ArrayList<String> getPicPaths() {
        return picPaths;
    }

    public void setPicPaths(ArrayList<String> picPaths) {
        this.picPaths = picPaths;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", name='" + name + '\'' +
                ", title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", price=" + price +
                ", views=" + views +
                ", available=" + available +
                ", picPaths=" + picPaths +
                ", user=" + "" +          // the user String has to be empty, otherwise when loading a product, hibernate calls the toString of Product, which calls the User toString method, which calls the Product toString method ... https://stackoverflow.com/questions/40266770/spring-jpa-bi-directional-cannot-evaluate-tostring
                '}';
    }
}