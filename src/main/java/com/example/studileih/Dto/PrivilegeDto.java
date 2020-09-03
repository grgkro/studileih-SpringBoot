package com.example.studileih.Dto;

import com.example.studileih.Entity.Role;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Collection;


@Data
@NoArgsConstructor
public class PrivilegeDto {


    private Long id;

    private String name;


    private Collection<RoleDto> roles;

    public PrivilegeDto(String name) {
        this.name = name;
    }


}