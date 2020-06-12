package com.example.studileih.Entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;

@Entity
@Table(name="address")
@Data
@NoArgsConstructor
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NonNull
    private String street;

    @OneToOne(cascade = {CascadeType.ALL})
    private Dorm dorm;

    public Address(@NonNull String street, Dorm dorm) {
        this.street = street;
        this.dorm = dorm;
    }
}
