package com.example.studileih.Dto;

import com.example.studileih.Entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.OneToMany;
import java.util.List;

@Data
@NoArgsConstructor
public class DormDto {

    private Long id;
    private String name;
    private Double lat;
    private Double lng;
    private String district;
}
