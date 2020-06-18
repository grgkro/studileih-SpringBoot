package com.example.studileih.Entity;

import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Getter
@MappedSuperclass  //A mapped superclass is not an entity, and there is no table for it. Instead, the mapping information is applied to the entities that inherit from it.
// Choose your inheritance strategy:
@Inheritance(strategy= InheritanceType.TABLE_PER_CLASS)  // The table per class strategy maps each entity to its own table which contains a column for each entity attribute. That makes the query for a specific entity class easy and efficient. But depending on the amounts of records in both tables, this might become a performance issue.
//@Inheritance(strategy=InheritanceType.JOINED) // The joined table approach maps each class of the inheritance hierarchy to its own database table. This sounds similar to the table per class strategy. But this time, also the abstract superclass BaseEntity gets mapped to a database table.
//@Inheritance(strategy=InheritanceType.SINGLE_TABLE)  // The single table strategy maps all entities of the inheritance structure to the same database table. This approach makes polymorphic queries very efficient and provides the best performance.
public abstract class BaseEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", updatable = false)  // durch nullable = false wird es zu Pflichtfeld
  //  @Column(name = "created_at", nullable = false, updatable = false)  // durch nullable = false wird es zu Pflichtfeld
    @CreatedDate
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
  //  @Column(name = "updated_at", nullable = false) // durch nullable = false wird es zu Pflichtfeld
    @LastModifiedDate
    private Date updatedAt;
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}