package com.vidasalud.bff;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class BffApplicationTests {

	// El bean JwtDecoder "real" (SecurityConfig#jwtDecoder) llama a
	// JwtDecoders.fromOidcIssuerLocation(...), que hace una petición HTTP a
	// Azure AD para descargar los metadatos OIDC/JWKS al levantar el contexto.
	// En un test de "contextLoads" no queremos depender de red ni de que el
	// tenant exista: lo reemplazamos por un mock para que el contexto levante
	// siempre, incluso sin conexión a internet (por ejemplo, en un pipeline de CI).
	@MockitoBean
	private JwtDecoder jwtDecoder;

	@Test
	void contextLoads() {
	}

}
