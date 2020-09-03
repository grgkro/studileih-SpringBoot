package com.example.studileih.Dto;

import com.example.studileih.Entity.Privilege;
import com.example.studileih.Entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Collection;

@Data
@NoArgsConstructor
public class RoleDto {

    private Long id;

    private String name;
    private Collection<UserDto> users;
    private Collection<PrivilegeDto> privileges;

    public RoleDto(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
//                ", name='" + name + '\'' +
//                ", users=" + users.size() +
//                ", privileges=" + privileges.size() +
                '}';
    }
}