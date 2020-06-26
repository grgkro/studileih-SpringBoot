package com.example.studileih.Entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;

@Entity
@Table(name="user")
@Data  // Project Lombock: erstellt automatisch alle Getter/Setter, eine ToString Methode, einen Konstruktor mit allen Pflichtfeldern... https://projectlombok.org/features/Data
@NoArgsConstructor // erstellt einfach nen leeren Konstruktor: public User() {}
public class User {
    @Id  // for more details why the ID is generated like this: https://stackoverflow.com/questions/62548985/hibernate-generates-one-id-for-two-tables-entities/62549190?noredirect=1#comment110616128_62549190
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_generator")
    @SequenceGenerator(name="user_generator", sequenceName = "user_seq", allocationSize=1)
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
    private String email;
    private String password;
    private String room;
    private String profilePic;

    //One user can have many products, so here we have a one-to-many mapping.
    //The way this works at the database level is we have user_id as a primary key in the user table and also a user_id as a foreign key in products.
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = {CascadeType.ALL})  //das cascadeType.ALL sorgt dafür, dass wenn der User gelöscht wird, alle zu ihm gehörenden Produkte auch gelöscht werden.
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