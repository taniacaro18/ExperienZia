package com.experienzia.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Convierto entidad ↔ DTO sin escribir mil setters a mano
@Configuration
public class ModelMapperConfig {

	@Bean
	public ModelMapper modelMapper() {
		ModelMapper mapper = new ModelMapper();
		// STRICT: si el DTO y la entidad no coinciden, mejor que falle que mapear mal
		mapper.getConfiguration()
				.setMatchingStrategy(MatchingStrategies.STRICT)
				.setSkipNullEnabled(true) 
				.setFieldMatchingEnabled(false) 
				.setAmbiguityIgnored(true); 
		return mapper;
	}
}
