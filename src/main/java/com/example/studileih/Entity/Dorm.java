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
@Table(name="dorm")
@Data
@NoArgsConstructor
public class Dorm {
    @Id  // for more details why the ID is generated like this: https://stackoverflow.com/questions/62548985/hibernate-generates-one-id-for-two-tables-entities/62549190?noredirect=1#comment110616128_62549190
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dorm_generator")
    @SequenceGenerator(name="dorm_generator", sequenceName = "dorm_seq", allocationSize=1)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    private String name;
//    @OneToMany(mappedBy = "dorm")
//    private List<User> users;

    private Double lat;
    private Double lng;
    private String district;

    public Dorm(String name, Double lat, Double lng, String district) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.district = district;
    }
}