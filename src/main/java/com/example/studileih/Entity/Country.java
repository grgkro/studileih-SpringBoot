package com.example.studileih.Entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="country")
@Data
public class Country extends BaseEntity {
    @NonNull private String name;
}
