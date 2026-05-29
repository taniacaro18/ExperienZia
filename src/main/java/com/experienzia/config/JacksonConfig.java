package com.experienzia.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.databind.ObjectMapper;

// Fechas LocalDateTime y demás serializan bien en JSON para el front
@Configuration
public class JacksonConfig {

	@Bean
	@Primary 
	public ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		// Sin esto LocalDateTime a veces sale mal en el JSON del front
		mapper.findAndRegisterModules();
		return mapper;
	}
}
