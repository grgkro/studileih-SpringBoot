package com.example.studileih.Repository;

import com.example.studileih.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	User findByName(String name);

	Boolean existsByName(String name);

	Boolean existsByEmail(String email);

    User findByEmail(String email);
}
