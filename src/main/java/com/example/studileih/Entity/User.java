package com.example.studileih.Entity;

import lombok.*;

import javax.persistence.*;
import java.nio.file.Path;
import java.util.List;

@Entity
@Table(name="user")
@Data  // Project Lombock: erstellt automatisch alle Getter/Settet, eine ToString Methode, einen Konstruktor mit allen Pflichtfeldern... https://projectlombok.org/features/Data
@NoArgsConstructor // erstellt leeren Konstruktor: public User() {}
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

    // Jeder User hat nur ein Wohnheim, aber ein Wohnheim hat viele User/Bewohner. -> ManyToOne. Hier wird das Dorm direkt in die user Tabelle geholt mit dem JoinColumn.
    // Weiter oben bei Products habe ich kein JoinColumn benutzt, weil es hier viele Produkte gibt, ich glaub dann geht das gar nicht?
    // Das Wohnheim soll nicht gelöscht werden, nur weil ein User gelöscht wird, daher kein cascade = {CascadeType.ALL}
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "dorm")
    private Dorm dorm;


    //Die verschiedenen Konstruktoren waren nötig, weil ich schnell unterschiedliche User testen wollte.
    // profilePic kann nicht im Konstrukor schon festgelegt werden, da es später erst vom Nutzer hochgeladen werden muss.
    public User(String name) {
        this.name = name;
    }

    public User(String name, String email, String password, List<Product> products, Dorm dorm) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.products = products;
        this.dorm = dorm;
    }


}