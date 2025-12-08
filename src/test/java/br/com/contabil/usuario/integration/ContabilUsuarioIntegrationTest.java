package br.com.contabil.usuario.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import br.com.contabil.usuario.entity.ContabilUsuarioEntity;
import br.com.contabil.usuario.repository.ContabilUsuarioRepository;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class ContabilUsuarioIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ContabilUsuarioRepository repository;

	@BeforeEach
	void setup() {
		repository.deleteAll();
	}

	// ----------------- POST -----------------
	@Test
	@DisplayName("POST - Criar usuário com sucesso")
	void testCreateUser() throws Exception {
		String userJson = """
				    {
				        "clerkId": "abc123",
				        "nome": "Davi",
				        "email": "davi@example.com",
				        "userImgSrc": "img",
				        "activeCourse": 0,
				        "hearts": 5,
				        "points": 10
				    }
				""";

		mockMvc.perform(MockMvcRequestBuilders.post("/contabil-usuario/v1/").contentType(MediaType.APPLICATION_JSON)
				.content(userJson)).andExpect(status().isCreated()).andExpect(jsonPath("$.data", is("abc123")))
				.andExpect(jsonPath("$.errors").doesNotExist());
	}

	@Test
	@DisplayName("POST - Deve retornar 400 BAD REQUEST")
	void testCreateUserBadRequest() throws Exception {
		String userJson = """
				    {
				        "clerkId": "abc124",
				        "nome": "Davi",
				        "email": null,
				        "userImgSrc": "img",
				        "activeCourse": 0,
				        "hearts": 5,
				        "points": 10
				    }
				""";

		mockMvc.perform(MockMvcRequestBuilders.post("/contabil-usuario/v1/").contentType(MediaType.APPLICATION_JSON)
				.content(userJson)).andExpect(status().isBadRequest());
	}

	// ----------------- GET All -----------------
	@Test
	@DisplayName("GET - Buscar todos usuários")
	void testFindAll() throws Exception {
		ContabilUsuarioEntity usuario = new ContabilUsuarioEntity();
		usuario.setClerkId("abc");
		usuario.setNome("Davi");
		usuario.setEmail("davi@example.com");
		usuario.setHearts(5);
		usuario.setPoints(10);

		repository.save(usuario);
		repository.flush();

		mockMvc.perform(MockMvcRequestBuilders.get("/contabil-usuario/v1/").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$.data", hasSize(1)))
				.andExpect(jsonPath("$.errors").doesNotExist());
	}

	@Test
	@DisplayName("GET - Nenhum usuário encontrado")
	void testFindAllNotFound() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/contabil-usuario/v1/").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound()).andExpect(jsonPath("$.data").doesNotExist())
				.andExpect(jsonPath("$.errors", is("Nenhum registro encontrado.")));
	}

	// ----------------- GET by clerkId -----------------
	@Test
	@DisplayName("GET - Buscar usuário por clerkId")
	void testFindByClerkId() throws Exception {
		ContabilUsuarioEntity usuario = new ContabilUsuarioEntity();
		usuario.setClerkId("abc");
		usuario.setNome("Davi");
		usuario.setEmail("davi@example.com");
		usuario.setHearts(5);
		usuario.setPoints(10);

		repository.save(usuario);
		repository.flush();

		mockMvc.perform(MockMvcRequestBuilders.get("/contabil-usuario/v1/abc/").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$.data.clerkId", is("abc")))
				.andExpect(jsonPath("$.errors").doesNotExist());
	}

	@Test
	@DisplayName("GET - Usuário por clerkId não encontrado")
	void testFindByClerkIdNotFound() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/contabil-usuario/v1/xyz/").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound()).andExpect(jsonPath("$.data").doesNotExist())
				.andExpect(jsonPath("$.errors", is("Nenhum registro encontrado.")));
	}

	// ----------------- PUT -----------------
	@Test
	@DisplayName("PUT - Atualizar pontos e hearts com sucesso")
	void testUpdatePointsAndHearts() throws Exception {
		ContabilUsuarioEntity usuario = new ContabilUsuarioEntity();
		usuario.setClerkId("abc");
		usuario.setNome("Davi");
		usuario.setEmail("davi@example.com");
		usuario.setHearts(5);
		usuario.setPoints(10);

		repository.save(usuario);
		repository.flush();

		mockMvc.perform(
				MockMvcRequestBuilders.put("/contabil-usuario/v1/abc/5/15").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$.data", is(1)))
				.andExpect(jsonPath("$.errors").doesNotExist());
	}

	@Test
	@DisplayName("PUT - Usuário não encontrado para atualização")
	void testUpdatePointsAndHeartsNotFound() throws Exception {
		mockMvc.perform(
				MockMvcRequestBuilders.put("/contabil-usuario/v1/xyz/5/15").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound()).andExpect(jsonPath("$.data").doesNotExist())
				.andExpect(jsonPath("$.errors", is("Usuário não encontrado para o clerkId: xyz")));
	}
}
