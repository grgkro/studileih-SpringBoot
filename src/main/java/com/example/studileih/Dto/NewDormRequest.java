package com.example.studileih.Dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NewDormRequest {
    private String name;
    private String city;
    private String street;
    private String houseNumber;
}
