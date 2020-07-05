package com.example.studileih.Repository;

import com.example.studileih.Entity.Dorm;
import com.example.studileih.Entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DormRepository extends JpaRepository<Dorm, Long> {
}
