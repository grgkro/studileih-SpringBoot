package com.example.studileih.Entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Table(name="privileges")
@Data
@NoArgsConstructor
public class Privilege {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    @ManyToMany(mappedBy = "privileges", fetch = FetchType.LAZY)
    private Collection<Role> roles;

    public Privilege(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Privilege{" +
                "id=" + id +
//                ", name='" + name + '\'' +
//                ", roles=" + roles.size() +
                '}';
    }
}