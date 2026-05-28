package com.experienzia;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Yo solo verifico que Spring arranque sin reventar (contextLoads)
@SpringBootTest
class ApplicationTests {

	// Si esto pasa, los beans y la config básica cargaron bien (no prueba negocio real)
	@Test
	void contextLoads() {
	}

}
