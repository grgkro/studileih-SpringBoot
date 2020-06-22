package com.example.studileih.Entity;

import lombok.*;

import javax.persistence.*;
import java.nio.file.Path;
import java.util.List;

@Entity
@Table(name="user")
@Data  // Project Lombock: erstellt automatisch alle Getter/Setter, eine ToString Methode, einen Konstruktor mit allen Pflichtfeldern... https://projectlombok.org/features/Data
@NoArgsConstructor // erstellt einfach nen leeren Konstruktor: public User() {}
public class User extends BaseEntity{

    private String name;
    private String email;
    private String password;
    private String room;
    private String profilePic;

    //One user can have many products, so here we have a one-to-many mapping.
    //The way this works at the database level is we have user_id as a primary key in the user table and also a user_id as a foreign key in products.
    @OneToMany(mappedBy = "user", cascade = {CascadeType.ALL})  //das cascadeType.ALL sorgt dafür, dass wenn der User gelöscht wird, alle zu ihm gehörenden Produkte auch gelöscht werden.
    private List<Product> products;

    private String dorm;
    private String city;


    //Die verschiedenen Konstruktoren sind nötig, weil ich schnell unterschiedliche User anlegen und testen wollte.
    // profilePic kann nicht im Konstrukor schon festgelegt werden, da es später erst vom Nutzer hochgeladen werden muss.
    public User(String name) {
        this.name = name;
    }

    public User(String name, String email, String password, List<Product> products, String dorm) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.products = products;
        this.dorm = dorm;
    }


}