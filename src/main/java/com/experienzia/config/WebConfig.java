package com.experienzia.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuración global del módulo Web.
 * - El CORS lo maneja SecurityConfig (junto con JWT).
 * - Aquí solo configuramos archivos estáticos: la carpeta uploads/ se sirve en /uploads/**
 */
@Configuration // clase de configuración de Spring (no es un controller)
public class WebConfig implements WebMvcConfigurer {

	/**
	 * Registra dónde buscar archivos cuando alguien pide /uploads/...
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// Exponemos el directorio físico "uploads/" como ruta pública "/uploads/**"
		// para que las imágenes/PDFs del comprobante de pago puedan visualizarse.
		Path uploadDir = Paths.get("uploads").toAbsolutePath().normalize();
		// location = ruta en disco convertida a URL que Spring entiende
		String location = uploadDir.toUri().toString();
		registry.addResourceHandler("/uploads/**")
				.addResourceLocations(location)
				.setCachePeriod(3600); // cache 1 hora en segundos (3600)
	}
}
