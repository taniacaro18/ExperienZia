package com.experienzia.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// los archivos que suben (comprobantes) desde /uploads/** para que el front los vea
@Configuration 
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {

		Path uploadDir = Paths.get("uploads").toAbsolutePath().normalize();
		String location = uploadDir.toUri().toString();
		registry.addResourceHandler("/uploads/**")
				.addResourceLocations(location)
				.setCachePeriod(3600); 
	}
}
