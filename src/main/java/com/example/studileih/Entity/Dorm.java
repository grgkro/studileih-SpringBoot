package com.example.studileih.Entity;

import com.example.studileih.Dto.DormDistricts;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name="dorms")
@Data
@NoArgsConstructor
public class Dorm {
    @Id  // for more details why the ID is generated like this: https://stackoverflow.com/questions/62548985/hibernate-generates-one-id-for-two-tables-entities/62549190?noredirect=1#comment110616128_62549190
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dorm_generator")
    @SequenceGenerator(name="dorm_generator", sequenceName = "dorm_seq", allocationSize=1)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    private String name;
    @OneToMany(mappedBy = "dorm")
    private List<User> users;

    private Double lat;
    private Double lng;
    private String city;
    private String district;

    public Dorm(String name, Double lat, Double lng, String city) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.city = city;
    }

    public Dorm(String name, Double lat, Double lng, String city, String district) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.city = city;
        this.district = district;
    }

    // gleiches Problem mit Rekursion wie bei Product.java -> Dorm enthält User enthält Dorm enthält...
    @Override
    public String toString() {
        return "Dorm{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", users=" + "" +
                ", lat=" + lat +
                ", lng=" + lng +
                ", city='" + city + '\'' +
                ", district='" + district + '\'' +
                '}';
    }
}