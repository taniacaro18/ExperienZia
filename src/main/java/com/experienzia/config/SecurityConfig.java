package com.experienzia.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.experienzia.security.JwtAuthenticationFilter;

// se define  quién entra sin token y quién necesita JWT (debe cuadrar con SecurityPaths)
@Configuration
@EnableWebSecurity 
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
	}

	// Hash de contraseñas en la BD (registro, admin por defecto, recuperar clave)
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	// UserDetails en memoria vacío porque yo autentico solo con JWT, no con form login
	@Bean
	public UserDetailsService jwtOnlyUserDetailsService() {
		return new InMemoryUserDetailsManager();
	}

	// CORS para que el front en localhost pegue al API sin que el navegador frene
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();

		config.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*"));
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setExposedHeaders(List.of("Authorization", "Content-Disposition"));
		config.setAllowCredentials(true); 
		config.setMaxAge(3600L); 
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config); 
		return source;
	}

	// Sin sesión en servidor: todo va con Bearer; login/registro/catálogo público sin token
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.cors(Customizer.withDefaults())
				.csrf(AbstractHttpConfigurer::disable) 
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
						.requestMatchers("/actuator/health", "/actuator/info").permitAll()
						.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
						.requestMatchers(HttpMethod.POST,
								"/api/usuarios/login",
								"/api/usuarios/registro",
								"/api/usuarios/recuperar").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/certificados/validar/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/certificados/pdf/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/eventos/catalogo/publicos", "/api/eventos/catalogo/publicos/*").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/export/admin/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/reportes/admin/**").hasRole("ADMIN")
						.anyRequest().authenticated())
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}
}
