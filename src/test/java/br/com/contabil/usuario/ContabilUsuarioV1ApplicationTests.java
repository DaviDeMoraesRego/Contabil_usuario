package br.com.contabil.usuario;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class ContabilUsuarioV1ApplicationTests {

	@MockitoBean
    JwtDecoder jwtDecoder;
	
	@Test
	void contextLoads() {
	}

}
