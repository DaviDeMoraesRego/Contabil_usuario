package br.com.contabil.usuario.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import br.com.contabil.usuario.dto.ContabilUsuarioDto;
import br.com.contabil.usuario.exception.BadRequestException;
import br.com.contabil.usuario.exception.NotFoundExceptionException;
import br.com.contabil.usuario.exception.internalServerError;
import br.com.contabil.usuario.service.ContabilUsuarioService;

@WebMvcTest(ContabilUsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)

@ContextConfiguration(classes = { ContabilUsuarioController.class, RestExceptionHandler.class })
public class ContabilUsuarioControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ContabilUsuarioService service;

	private static final HttpHeaders HEADERS = new HttpHeaders();

	@BeforeAll
	public static void setup() {
		HEADERS.add("Content-Type", "application/json;charset=UTF-8");
		HEADERS.add("Accept", "application/json;charset=UTF-8");
	}

	// ---------------------------------------------------------
	// POST /contabil-usuario/v1/
	// ---------------------------------------------------------
	@Test
	@DisplayName("POST - Deve retornar 201 CREATED ao criar usuário")
	void shouldReturnCreated_whenCreateUser() throws Exception {
		Mockito.when(service.createOrUpdate(Mockito.any())).thenReturn("abc123");

		MvcResult response = mockMvc
				.perform(MockMvcRequestBuilders.post("/contabil-usuario/v1/").headers(HEADERS).content("""
						    {
						        "clerkId": "abc123",
						        "nome": "Davi",
						        "email": "davi@example.com",
						        "userImgSrc": "img",
						        "activeCourse": 0,
						        "hearts": 5,
						        "points": 10
						    }
						""")).andExpect(status().isCreated()).andReturn();

		String responseBody = response.getResponse().getContentAsString();
		assertNotNull(responseBody);
		assertEquals("{\"data\":\"abc123\",\"errors\":null}", responseBody);
	}

	@Test
	@DisplayName("POST - Deve retornar 400 BAD REQUEST")
	void shouldReturnBadRequest_whenServiceThrowsBadRequest() throws Exception {
		Mockito.when(service.createOrUpdate(Mockito.any())).thenThrow(new BadRequestException("Erro no DTO"));

		MvcResult response = mockMvc
				.perform(MockMvcRequestBuilders.post("/contabil-usuario/v1/").headers(HEADERS)
						.content("{\"clerkId\":\"abc\",\"email\":\"davi@example.com\"}"))
				.andExpect(status().isBadRequest()).andReturn();

		String responseBody = response.getResponse().getContentAsString();
		assertEquals("{\"data\":null,\"errors\":\"Erro no DTO\"}", responseBody);
	}

	@Test
	@DisplayName("POST - Deve retornar 500 INTERNAL SERVER errors")
	void shouldReturnInternalServererrors_whenServiceThrowsInternal() throws Exception {
		Mockito.when(service.createOrUpdate(Mockito.any())).thenThrow(new internalServerError("Erro interno"));

		MvcResult response = mockMvc
				.perform(MockMvcRequestBuilders.post("/contabil-usuario/v1/").headers(HEADERS)
						.content("{\"clerkId\":\"abc\",\"email\":\"davi@example.com\"}"))
				.andExpect(status().isInternalServerError()).andReturn();

		String responseBody = response.getResponse().getContentAsString();
		assertEquals("{\"data\":null,\"errors\":\"Erro interno\"}", responseBody);
	}

	// ---------------------------------------------------------
	// GET /contabil-usuario/v1/
	// ---------------------------------------------------------
	@Test
	@DisplayName("GET All - Deve retornar 200 OK")
	void shouldReturnOk_whenFindAll() throws Exception {
		Mockito.when(service.findAll())
				.thenReturn(List.of(new ContabilUsuarioDto("abc", "Davi", "davi@example.com", null, 0, 5, 10)));

		MvcResult response = mockMvc.perform(MockMvcRequestBuilders.get("/contabil-usuario/v1/").headers(HEADERS))
				.andExpect(status().isOk()).andReturn();

		String responseBody = response.getResponse().getContentAsString();
		assertEquals(
				"{\"data\":[{\"clerkId\":\"abc\",\"nome\":\"Davi\",\"email\":\"davi@example.com\",\"userImgSrc\":null,\"activeCourse\":0,\"hearts\":5,\"points\":10}],\"errors\":null}",
				responseBody);
	}

	@Test
	@DisplayName("GET All - Deve retornar 404 NOT FOUND")
	void shouldReturnNotFound_whenFindAllThrows() throws Exception {
		Mockito.when(service.findAll()).thenThrow(new NotFoundExceptionException("Nenhum registro encontrado."));

		MvcResult response = mockMvc.perform(MockMvcRequestBuilders.get("/contabil-usuario/v1/").headers(HEADERS))
				.andExpect(status().isNotFound()).andReturn();

		String responseBody = response.getResponse().getContentAsString();
		assertEquals("{\"data\":null,\"errors\":\"Nenhum registro encontrado.\"}", responseBody);
	}

	// ---------------------------------------------------------
	// GET /contabil-usuario/v1/{clerkId}/
	// ---------------------------------------------------------
	@Test
	@DisplayName("GET ByClerkId - Deve retornar 200 OK")
	void shouldReturnOk_whenFindByClerkId() throws Exception {
		Mockito.when(service.findByClerkId("abc"))
				.thenReturn(new ContabilUsuarioDto("abc", "Davi", "davi@example.com", null, 0, 5, 10));

		MvcResult response = mockMvc.perform(MockMvcRequestBuilders.get("/contabil-usuario/v1/abc/").headers(HEADERS))
				.andExpect(status().isOk()).andReturn();

		String responseBody = response.getResponse().getContentAsString();
		assertEquals(
				"{\"data\":{\"clerkId\":\"abc\",\"nome\":\"Davi\",\"email\":\"davi@example.com\",\"userImgSrc\":null,\"activeCourse\":0,\"hearts\":5,\"points\":10},\"errors\":null}",
				responseBody);
	}

	@Test
	@DisplayName("GET ByClerkId - Deve retornar 404 NOT FOUND")
	void shouldReturnNotFound_whenFindByClerkIdThrows() throws Exception {
		Mockito.when(service.findByClerkId("abc"))
				.thenThrow(new NotFoundExceptionException("Nenhum registro encontrado."));

		MvcResult response = mockMvc.perform(MockMvcRequestBuilders.get("/contabil-usuario/v1/abc/").headers(HEADERS))
				.andExpect(status().isNotFound()).andReturn();

		String responseBody = response.getResponse().getContentAsString();
		assertEquals("{\"data\":null,\"errors\":\"Nenhum registro encontrado.\"}", responseBody);
	}

	// ---------------------------------------------------------
	// PUT /contabil-usuario/v1/{clerkId}/{hearts}/{points}
	// ---------------------------------------------------------
	@Test
	@DisplayName("PUT - Deve retornar 200 OK")
	void shouldReturnOk_whenUpdate() throws Exception {
		Mockito.when(service.updatePointsAndHearts("abc", 5, 10)).thenReturn(1);

		MvcResult response = mockMvc
				.perform(MockMvcRequestBuilders.put("/contabil-usuario/v1/abc/5/10").headers(HEADERS))
				.andExpect(status().isOk()).andReturn();

		String responseBody = response.getResponse().getContentAsString();
		assertEquals("{\"data\":1,\"errors\":null}", responseBody);
	}

	@Test
	@DisplayName("PUT - Deve retornar 404 NOT FOUND")
	void shouldReturnNotFound_whenUpdateThrows() throws Exception {
		Mockito.when(service.updatePointsAndHearts("abc", 5, 10))
				.thenThrow(new NotFoundExceptionException("Usuário não encontrado"));

		MvcResult response = mockMvc
				.perform(MockMvcRequestBuilders.put("/contabil-usuario/v1/abc/5/10").headers(HEADERS))
				.andExpect(status().isNotFound()).andReturn();

		String responseBody = response.getResponse().getContentAsString();
		assertEquals("{\"data\":null,\"errors\":\"Usuário não encontrado\"}", responseBody);
	}

	@Test
	@DisplayName("PUT - Deve retornar 500 INTERNAL SERVER errors")
	void shouldReturnInternalerrors_whenUpdateThrowsInternal() throws Exception {
		Mockito.when(service.updatePointsAndHearts("abc", 5, 10)).thenThrow(new internalServerError("Erro interno"));

		MvcResult response = mockMvc
				.perform(MockMvcRequestBuilders.put("/contabil-usuario/v1/abc/5/10").headers(HEADERS))
				.andExpect(status().isInternalServerError()).andReturn();

		String responseBody = response.getResponse().getContentAsString();
		assertEquals("{\"data\":null,\"errors\":\"Erro interno\"}", responseBody);
	}
}
