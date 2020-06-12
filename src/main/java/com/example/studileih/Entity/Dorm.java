package com.example.studileih.Entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="dorm")
@Data
@NoArgsConstructor
public class Dorm extends BaseEntity {
    @NonNull private String name;
    @OneToMany(mappedBy = "dorm", cascade = {CascadeType.ALL})
    private List<User> users;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address")
    private Address address;

    public Dorm(@NonNull String name) {
        this.name = name;
    }
}
