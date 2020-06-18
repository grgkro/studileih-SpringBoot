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

    // an jeder Adresse gibt es ein Wohnheim und jedes Wohnheim hat nur eine Adresse
    @OneToOne(cascade = {CascadeType.ALL}) //das cascadeType.ALL sorgt dafür, dass wenn das Wohnheim gelöscht wird, die zu ihm gehörenden Adresse auch gelöscht wird.
    private Dorm dorm;

    public Address(@NonNull String street, Dorm dorm) {  //@NonNull -> da jedes Wohnheim eine Adresse hat, muss das eingegeben werden
        this.street = street;
        this.dorm = dorm;
    }
}
