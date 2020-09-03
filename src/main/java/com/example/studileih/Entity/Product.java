package com.example.studileih.Entity;

import com.example.studileih.Service.ProductBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;

@Entity
@Table(name="products")
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

     private String description;
     private String title;
     private String type;
     private double price = 0;
     private boolean isBeerOk;  // when creating a new product, the user can chose that instead of a price, the renter can also give one beer.
     private int views = 0; //How often was the product viewed?
     private boolean available = true; //is it available?

    private String category;

    // the owner can specify, when is a good time for renting the product?
    private Date startDay;
    private Date endDay;
    private String pickUpTime;
    private String returnTime;

    @Lob
    // wir sagen der DB, dass sie diese Spalte als Datentyp LongText (Large Object = lob) anlegen soll, damit auch längere Texte rein gehen. Sonst legt sie es automatich als TINYBLOB an und das ist schnell zu klein.
    private ArrayList<String> picPaths;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // the next two properties are used to display the city and dorm of the products from the other dorms on the main page (products component) -> extra loading the dorm and city with the userId of the product is too complicated here.
    private String dorm;
    private String city;


    public Product(ProductBuilder builder) {
        this.id = builder.getId();
        this.createdAt = builder.getCreatedAt();
        this.updatedAt = builder.getUpdatedAt();
        this.description = builder.getDescription();
        this.title = builder.getTitle();
        this.price = builder.getPrice();
        this.isBeerOk = builder.isBeerOk();
        this.views = builder.getViews();
        this.available = builder.isAvailable();
        this.category = builder.getCategory();
        this.startDay = builder.getStartDay();
        this.endDay = builder.getEndDay();
        this.pickUpTime = builder.getPickUpTime();
        this.returnTime = builder.getReturnTime();
        this.picPaths = builder.getPicPaths();
        this.user = builder.getUser();
        this.dorm = builder.getDorm();
        this.city = builder.getCity();
    }



    @Override
    public String toString() {
        return "Product{" +
                "id=" + id;
//                ", createdAt=" + createdAt +
//                ", updatedAt=" + updatedAt +
//                ", description='" + description + '\'' +
//                ", title='" + title + '\'' +
//                ", type='" + type + '\'' +
//                ", price=" + price +
//                ", views=" + views +
//                ", available=" + available +
//                ", picPaths=" + picPaths +
//                ", user=" + "" +          // the user String has to be empty, otherwise when loading a product, hibernate calls the toString of Product, which calls the User toString method, which calls the Product toString method ... https://stackoverflow.com/questions/40266770/spring-jpa-bi-directional-cannot-evaluate-tostring
//                ", dorm=" + dorm +
//                ", city=" + city +
//                '}';
    }
}