package com.experienzia;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Prueba automática de Spring Boot.
 * Solo verifica que el contexto de la aplicación arranca sin errores.
 */
@SpringBootTest
class ApplicationTests {

	@Test
	/** Comprueba que Spring carga todos los beans. */
	void contextLoads() {
	}

}
