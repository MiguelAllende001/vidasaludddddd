package com.vidasalud.report;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ReportApplicationTests {

	// El listener de Kafka arranca en un hilo aparte y reintenta la conexion
	// solo, asi que el contexto levanta igual sin un broker corriendo.
	@Test
	void contextLoads() {
	}

}
