package com.example.studileih.Service;

import com.example.studileih.Entity.Product;
import com.example.studileih.Entity.User;
import java.util.ArrayList;
import java.util.Date;

// fluent interface design pattern -> used because Entity Product was growing too large and needed too many different constructors to maintain!
// beste Erklärung: https://www.digicomp.ch/blog/2017/12/07/das-builder-pattern
// https://stackoverflow.com/questions/40264/how-many-constructor-arguments-is-too-many
public class ProductBuilder {


    private Long id;
    private Date createdAt;
    private Date updatedAt;
    private String description;
    private String title;
    private double price = 0;
    private boolean isBeerOk;  // when creating a new product, the user can chose that instead of a price, the renter can also give one beer.
    private int views = 0; //How often was the product viewed?
    private boolean available = true; //is it available?
    private String category;
    private Date startDay;
    private Date endDay;
    private String pickUpTime;
    private String returnTime;
    private ArrayList<String> picPaths;
    private User user;
    private String dorm;
    private String city;


        public ProductBuilder withCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
        return this;
        }
        
    public ProductBuilder withUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }
    public ProductBuilder withDescription(String description) {
        this.description = description;
        return this;
    }
    public ProductBuilder withTitle(String title) {
        this.title = title;
        return this;
    }
    public ProductBuilder withPrice(double price) {
        this.price = price;
        return this;
    }
    public ProductBuilder withIsBeerOk(boolean isBeerOk) {
        this.isBeerOk = isBeerOk;
        return this;
    }
    public ProductBuilder withViews(int views) {
        this.views = views;
        return this;
    }
    public ProductBuilder withAvailable(boolean available) {
        this.available = available;
        return this;
    }
    public ProductBuilder withCategory(String category) {
        this.category = category;
        return this;
    }
    public ProductBuilder withStartDay(Date startDay) {
        this.startDay = startDay;
        return this;
    }
    public ProductBuilder withEndDay(Date endDay) {
        this.endDay = endDay;
        return this;
    }
    public ProductBuilder withPickUpTime(String pickUpTime) {
        this.pickUpTime = pickUpTime;
        return this;
    }
    public ProductBuilder withReturnTime(String returnTime) {
        this.returnTime = returnTime;
        return this;
    }
    public ProductBuilder withPicPaths(ArrayList<String> picPaths) {
        this.picPaths = picPaths;
        return this;
    }
    public ProductBuilder withUser(User user) {
        this.user = user;
        return this;
    }
    public ProductBuilder withDorm(String dorm) {
        this.dorm = dorm;
        return this;
    }
    public ProductBuilder withCity(String city) {
        this.city = city;
        return this;
    }

    public Long getId() {
        return id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public double getPrice() {
        return price;
    }

    public boolean isBeerOk() {
        return isBeerOk;
    }

    public int getViews() {
        return views;
    }

    public boolean isAvailable() {
        return available;
    }

    public String getCategory() {
        return category;
    }

    public Date getStartDay() {
        return startDay;
    }

    public Date getEndDay() {
        return endDay;
    }

    public String getPickUpTime() {
        return pickUpTime;
    }

    public String getReturnTime() {
        return returnTime;
    }

    public ArrayList<String> getPicPaths() {
        return picPaths;
    }

    public User getUser() {
        return user;
    }

    public String getDorm() { return dorm;    }

    public String getCity() { return city; }

    // client doesn't get to instantiate Customer directly
        public Product build() {
            return new Product(this);
        }

    }

