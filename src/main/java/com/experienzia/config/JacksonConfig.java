package com.experienzia.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Crea el ObjectMapper de Jackson como bean de Spring.
 * A veces Spring Boot no lo registra solo y otros servicios lo necesitan
 * (por ejemplo para guardar/leer JSON de novedades de eventos).
 */
@Configuration
public class JacksonConfig {

	/**
	 * ObjectMapper principal: convierte objetos Java ↔ JSON.
	 */
	@Bean
	@Primary // si hay varios ObjectMapper, este es el que usa Spring por defecto
	public ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		// registra módulos extra (fechas Java 8, etc.) si están en el classpath
		mapper.findAndRegisterModules();
		return mapper;
	}
}
