package com.example.studileih.Entity;

import lombok.Data;
import lombok.NonNull;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="city")
@Data
public class City extends BaseEntity {
    @NonNull private String name;
}
