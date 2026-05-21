package com.experienzia.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configura ModelMapper para copiar datos entre DTOs y entidades JPA.
 * Nos ahorra escribir setters a mano en muchos casos.
 */
@Configuration
public class ModelMapperConfig {

	/**
	 * Bean ModelMapper con reglas estrictas para evitar mapeos raros.
	 */
	@Bean
	public ModelMapper modelMapper() {
		ModelMapper mapper = new ModelMapper();
		// Estrategia estricta para evitar ambigüedades cuando una entidad tiene
		// tanto el id (Long) como la relación @ManyToOne (objeto) con el mismo nombre.
		mapper.getConfiguration()
				.setMatchingStrategy(MatchingStrategies.STRICT)
				.setSkipNullEnabled(true) // no pisa campos destino si el origen es null
				.setFieldMatchingEnabled(false) // solo getters/setters, no campos directos
				.setAmbiguityIgnored(true); // ignora si hay dos formas de mapear lo mismo
		return mapper;
	}
}
