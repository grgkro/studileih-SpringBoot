package com.example.studileih;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdditionalSpringConfiguration {
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}

