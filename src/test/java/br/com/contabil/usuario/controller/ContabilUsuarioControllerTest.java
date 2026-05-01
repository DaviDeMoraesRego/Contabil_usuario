package br.com.contabil.usuario.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.contabil.usuario.dto.ContabilUsuarioDto;
import br.com.contabil.usuario.exception.BadRequestException;
import br.com.contabil.usuario.exception.InternalServerError;
import br.com.contabil.usuario.exception.NotFoundException;
import br.com.contabil.usuario.service.ContabilUsuarioService;

@WebMvcTest(controllers = { ContabilUsuarioController.class, RestExceptionHandler.class })
@DisplayName("ContabilUsuarioController - Testes de Integração")
class ContabilUsuarioControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private ContabilUsuarioService service;

	private ContabilUsuarioDto dto;

	private static final String BASE_URL = "/contabil-usuario/v1";

	@BeforeEach
	void setUp() {
		dto = new ContabilUsuarioDto("clerk_001", "João", "joao@email.com", "http://img.png", 1, 5, 100);
	}

	// ─────────────────────────────────────────────────────────────────────────
	// POST /contabil-usuario/v1
	// ─────────────────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("POST /contabil-usuario/v1")
	class Create {

		@Test
        @WithMockUser
        @DisplayName("Deve retornar 201 e clerkId ao criar usuário com sucesso")
        void deveRetornar201_quandoCriarComSucesso() throws Exception {
            when(service.createOrUpdate(any())).thenReturn("clerk_001");

            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data").value("clerk_001"))
                    .andExpect(jsonPath("$.errors").doesNotExist());
        }

		@Test
		@WithMockUser
		@DisplayName("Deve retornar 400 quando clerkId está em branco")
		void deveRetornar400_quandoClerkIdEmBranco() throws Exception {
			dto.setClerkId("");

			mockMvc.perform(post(BASE_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(dto))).andExpect(status().isBadRequest());
		}

		@Test
		@WithMockUser
		@DisplayName("Deve retornar 400 quando email está em branco")
		void deveRetornar400_quandoEmailEmBranco() throws Exception {
			dto.setEmail("");

			mockMvc.perform(post(BASE_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(dto))).andExpect(status().isBadRequest());
		}

		@Test
		@WithMockUser
		@DisplayName("Deve retornar 400 quando hearts é negativo")
		void deveRetornar400_quandoHeartsNegativo() throws Exception {
			dto.setHearts(-1);

			mockMvc.perform(post(BASE_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(dto))).andExpect(status().isBadRequest());
		}

		@Test
		@WithMockUser
		@DisplayName("Deve retornar 400 quando points é negativo")
		void deveRetornar400_quandoPointsNegativo() throws Exception {
			dto.setPoints(-1);

			mockMvc.perform(post(BASE_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(dto))).andExpect(status().isBadRequest());
		}

		@Test
        @WithMockUser
        @DisplayName("Deve retornar 400 quando service lança BadRequestException")
        void deveRetornar400_quandoServiceLancaBadRequest() throws Exception {
            when(service.createOrUpdate(any())).thenThrow(new BadRequestException("argumento inválido"));

            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors").value("argumento inválido"));
        }

		@Test
        @WithMockUser
        @DisplayName("Deve retornar 500 quando service lança InternalServerError")
        void deveRetornar500_quandoServiceLancaInternalServerError() throws Exception {
            when(service.createOrUpdate(any())).thenThrow(new InternalServerError("erro interno"));

            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.errors").value("erro interno"));
        }
	}

	// ─────────────────────────────────────────────────────────────────────────
	// GET /contabil-usuario/v1
	// ─────────────────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("GET /contabil-usuario/v1")
	class FindAll {

		@Test
        @WithMockUser
        @DisplayName("Deve retornar 200 e lista de usuários com sucesso")
        void deveRetornar200_quandoHouverUsuarios() throws Exception {
            when(service.findAll()).thenReturn(List.of(dto));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].clerkId").value("clerk_001"))
                    .andExpect(jsonPath("$.data[0].email").value("joao@email.com"))
                    .andExpect(jsonPath("$.errors").doesNotExist());
        }

		@Test
        @WithMockUser
        @DisplayName("Deve retornar 404 quando não há usuários")
        void deveRetornar404_quandoSemUsuarios() throws Exception {
            when(service.findAll()).thenThrow(new NotFoundException("Nenhum registro encontrado."));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errors").value("Nenhum registro encontrado."));
        }

		@Test
        @WithMockUser
        @DisplayName("Deve retornar 500 quando service lança InternalServerError")
        void deveRetornar500_quandoErroInterno() throws Exception {
            when(service.findAll()).thenThrow(new InternalServerError("erro interno"));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.errors").value("erro interno"));
        }
	}

	// ─────────────────────────────────────────────────────────────────────────
	// GET /contabil-usuario/v1/{clerkId}/
	// ─────────────────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("GET /contabil-usuario/v1/{clerkId}/")
	class FindByClerkId {

		@Test
        @WithMockUser
        @DisplayName("Deve retornar 200 e usuário encontrado")
        void deveRetornar200_quandoEncontrado() throws Exception {
            when(service.findByClerkId("clerk_001")).thenReturn(dto);

            mockMvc.perform(get(BASE_URL + "/clerk_001/"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.clerkId").value("clerk_001"))
                    .andExpect(jsonPath("$.data.nome").value("João"))
                    .andExpect(jsonPath("$.errors").doesNotExist());
        }

		@Test
        @WithMockUser
        @DisplayName("Deve retornar 404 quando usuário não encontrado")
        void deveRetornar404_quandoNaoEncontrado() throws Exception {
            when(service.findByClerkId("clerk_999"))
                    .thenThrow(new NotFoundException("Nenhum registro encontrado."));

            mockMvc.perform(get(BASE_URL + "/clerk_999/"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errors").value("Nenhum registro encontrado."));
        }

		@Test
        @WithMockUser
        @DisplayName("Deve retornar 500 quando service lança InternalServerError")
        void deveRetornar500_quandoErroInterno() throws Exception {
            when(service.findByClerkId(any())).thenThrow(new InternalServerError("erro interno"));

            mockMvc.perform(get(BASE_URL + "/clerk_001/"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.errors").value("erro interno"));
        }
	}

	// ─────────────────────────────────────────────────────────────────────────
	// PUT /contabil-usuario/v1/{clerkId}/{hearts}/{points}
	// ─────────────────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("PUT /contabil-usuario/v1/{clerkId}/{hearts}/{points}")
	class Update {

		@Test
        @WithMockUser
        @DisplayName("Deve retornar 200 quando atualização bem-sucedida")
        void deveRetornar200_quandoAtualizacaoBemSucedida() throws Exception {
            when(service.updatePointsAndHearts("clerk_001", 3, 200)).thenReturn(1);

            mockMvc.perform(put(BASE_URL + "/clerk_001/3/200").with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(1))
                    .andExpect(jsonPath("$.errors").doesNotExist());
        }

		@Test
        @WithMockUser
        @DisplayName("Deve retornar 404 quando usuário não encontrado")
        void deveRetornar404_quandoNaoEncontrado() throws Exception {
            when(service.updatePointsAndHearts("clerk_999", 3, 200))
                    .thenThrow(new NotFoundException("Usuário não encontrado para o clerkId: clerk_999"));

            mockMvc.perform(put(BASE_URL + "/clerk_999/3/200").with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errors").value("Usuário não encontrado para o clerkId: clerk_999"));
        }

		@Test
        @WithMockUser
        @DisplayName("Deve retornar 500 quando service lança InternalServerError")
        void deveRetornar500_quandoErroInterno() throws Exception {
            when(service.updatePointsAndHearts(any(), anyInt(), anyInt()))
                    .thenThrow(new InternalServerError("erro interno"));

            mockMvc.perform(put(BASE_URL + "/clerk_001/3/200").with(csrf()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.errors").value("erro interno"));
        }
	}

	// ─────────────────────────────────────────────────────────────────────────
	// GET /contabil-usuario/v1/ranking/
	// ─────────────────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("GET /contabil-usuario/v1/ranking/")
	class FindRanking {

		@Test
        @WithMockUser
        @DisplayName("Deve retornar 200 e lista de ranking com sucesso")
        void deveRetornar200_quandoHouverRanking() throws Exception {
            when(service.findTop200ByOrderByPointsDesc(any(Pageable.class))).thenReturn(List.of(dto));

            mockMvc.perform(get(BASE_URL + "/ranking/")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].clerkId").value("clerk_001"))
                    .andExpect(jsonPath("$.errors").doesNotExist());
        }

		@Test
        @WithMockUser
        @DisplayName("Deve retornar 404 quando ranking vazio")
        void deveRetornar404_quandoRankingVazio() throws Exception {
            when(service.findTop200ByOrderByPointsDesc(any(Pageable.class)))
                    .thenThrow(new NotFoundException("Nenhum registro encontrado."));

            mockMvc.perform(get(BASE_URL + "/ranking/")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errors").value("Nenhum registro encontrado."));
        }

		@Test
        @WithMockUser
        @DisplayName("Deve retornar 500 quando service lança InternalServerError")
        void deveRetornar500_quandoErroInterno() throws Exception {
            when(service.findTop200ByOrderByPointsDesc(any(Pageable.class)))
                    .thenThrow(new InternalServerError("erro interno"));

            mockMvc.perform(get(BASE_URL + "/ranking/")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.errors").value("erro interno"));
        }
	}

	// ─────────────────────────────────────────────────────────────────────────
	// GET /contabil-usuario/v1/rank/{clerkId}/
	// ─────────────────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("GET /contabil-usuario/v1/rank/{clerkId}/")
	class FindUserRank {

		@Test
        @WithMockUser
        @DisplayName("Deve retornar 200 e rank do usuário com sucesso")
        void deveRetornar200_quandoRankEncontrado() throws Exception {
            when(service.findUserRank("clerk_001")).thenReturn(3);

            mockMvc.perform(get(BASE_URL + "/rank/clerk_001/"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(3))
                    .andExpect(jsonPath("$.errors").doesNotExist());
        }

		@Test
        @WithMockUser
        @DisplayName("Deve retornar 404 quando rank não encontrado")
        void deveRetornar404_quandoNaoEncontrado() throws Exception {
            when(service.findUserRank("clerk_999"))
                    .thenThrow(new NotFoundException("Ranking do usuario não encontrado."));

            mockMvc.perform(get(BASE_URL + "/rank/clerk_999/"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errors").value("Ranking do usuario não encontrado."));
        }

		@Test
        @WithMockUser
        @DisplayName("Deve retornar 500 quando service lança InternalServerError")
        void deveRetornar500_quandoErroInterno() throws Exception {
            when(service.findUserRank(any())).thenThrow(new InternalServerError("erro interno"));

            mockMvc.perform(get(BASE_URL + "/rank/clerk_001/"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.errors").value("erro interno"));
        }
	}
}