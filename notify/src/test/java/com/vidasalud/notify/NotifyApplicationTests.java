package com.vidasalud.notify;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NotifyApplicationTests {

	// Nota: la declaracion de colas/exchanges de RabbitAdmin es perezosa
	// (solo ocurre cuando se establece una conexion real), asi que este test
	// levanta el contexto igual aunque no haya un broker RabbitMQ corriendo.
	@Test
	void contextLoads() {
	}

}
