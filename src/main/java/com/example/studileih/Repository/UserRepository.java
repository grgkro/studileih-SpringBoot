package com.example.studileih.Repository;

import com.example.studileih.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

//	User findByName(String name);

	Boolean existsByName(String name);

	Boolean existsByEmail(String email);

    User findByEmail(String email);

    //https://www.baeldung.com/hibernate-initialize-proxy-exception
	@Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.name = :name")
	User findByName(@Param("name") String name);
}
