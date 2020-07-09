package com.example.studileih;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@EnableEncryptableProperties
public class StudileihApplication {

	public static void main(String[] args) {
		SpringApplication.run(StudileihApplication.class, args);
	}

}
