package br.com.contabil.usuario.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.PageRequest;

import br.com.contabil.usuario.adapter.ContabilUsuarioAdapter;
import br.com.contabil.usuario.dto.ContabilUsuarioDto;
import br.com.contabil.usuario.entity.ContabilUsuarioEntity;
import br.com.contabil.usuario.exception.BadRequestException;
import br.com.contabil.usuario.exception.InternalServerError;
import br.com.contabil.usuario.exception.NotFoundException;
import br.com.contabil.usuario.repository.ContabilUsuarioRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContabilUsuarioService - Testes Unitários")
class ContabilUsuarioServiceTest {

	@Mock
	private ContabilUsuarioRepository repository;

	@Mock
	private ContabilUsuarioAdapter adapter;

	@InjectMocks
	private ContabilUsuarioService service;

	private ContabilUsuarioDto dto;
	private ContabilUsuarioEntity entity;

	@BeforeEach
	void setUp() {
		dto = new ContabilUsuarioDto("clerk_001", "João", "joao@email.com", "http://img.png", 1, 5, 100);

		entity = new ContabilUsuarioEntity();
		entity.setClerkId("clerk_001");
		entity.setNome("João");
		entity.setEmail("joao@email.com");
		entity.setUserImgSrc("http://img.png");
		entity.setActiveCourse(1);
		entity.setHearts(5);
		entity.setPoints(100);
	}

	// ─────────────────────────────────────────────────────────────────────────
	// createOrUpdate
	// ─────────────────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("createOrUpdate")
	class CreateOrUpdate {

		@Test
        @DisplayName("Deve salvar e retornar o clerkId com sucesso")
        void deveSalvarERetornarClerkId() throws Exception {
            when(adapter.adapterDtoToEntity(eq(dto), eq(dto.getClerkId()))).thenReturn(entity);
            when(repository.save(entity)).thenReturn(entity);

            String result = service.createOrUpdate(dto);

            assertThat(result).isEqualTo("clerk_001");
            verify(repository, times(1)).save(entity);
        }

		@Test
        @DisplayName("Deve lançar BadRequestException quando IllegalArgumentException ocorrer")
        void deveLancarBadRequest_quandoIllegalArgument() {
            when(adapter.adapterDtoToEntity(eq(dto), eq(dto.getClerkId())))
                    .thenThrow(new IllegalArgumentException("argumento inválido"));

            assertThatThrownBy(() -> service.createOrUpdate(dto))
                    .isInstanceOf(BadRequestException.class);
        }

		@Test
        @DisplayName("Deve lançar BadRequestException quando OptimisticLockingFailureException ocorrer")
        void deveLancarBadRequest_quandoOptimisticLocking() {
            when(adapter.adapterDtoToEntity(eq(dto), eq(dto.getClerkId()))).thenReturn(entity);
            when(repository.save(entity))
                    .thenThrow(new OptimisticLockingFailureException("conflito de versão"));

            assertThatThrownBy(() -> service.createOrUpdate(dto))
                    .isInstanceOf(BadRequestException.class);
        }

		@Test
        @DisplayName("Deve lançar InternalServerErrorException para qualquer outra exceção")
        void deveLancarInternalServerError_quandoExcecaoGenerica() {
            when(adapter.adapterDtoToEntity(eq(dto), eq(dto.getClerkId())))
                    .thenThrow(new RuntimeException("erro inesperado"));

            assertThatThrownBy(() -> service.createOrUpdate(dto))
                    .isInstanceOf(InternalServerError.class);
        }
	}

	// ─────────────────────────────────────────────────────────────────────────
	// findAll
	// ─────────────────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("findAll")
	class FindAll {

		@Test
        @DisplayName("Deve retornar lista de DTOs com sucesso")
        void deveRetornarListaDeDtos() throws Exception {
            when(repository.findAll()).thenReturn(List.of(entity));
            when(adapter.adapterEntityToDto(entity)).thenReturn(dto);

            List<ContabilUsuarioDto> result = service.findAll();

            assertThat(result)
                    .hasSize(1)
                    .first()
                    .extracting(ContabilUsuarioDto::getClerkId)
                    .isEqualTo("clerk_001");

            verify(adapter, times(1)).adapterEntityToDto(entity);
        }

		@Test
        @DisplayName("Deve lançar NotFoundException quando não há registros")
        void deveLancarNotFoundException_quandoListaVazia() {
            when(repository.findAll()).thenReturn(Collections.emptyList());

            assertThatThrownBy(() -> service.findAll())
                    .isInstanceOf(NotFoundException.class);
        }

		@Test
        @DisplayName("Deve lançar InternalServerErrorException para qualquer outra exceção")
        void deveLancarInternalServerError_quandoExcecaoGenerica() {
            when(repository.findAll()).thenThrow(new RuntimeException("erro inesperado"));

            assertThatThrownBy(() -> service.findAll())
                    .isInstanceOf(InternalServerError.class);
        }
	}

	// ─────────────────────────────────────────────────────────────────────────
	// findByClerkId
	// ─────────────────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("findByClerkId")
	class FindByClerkId {

		@Test
        @DisplayName("Deve retornar DTO quando usuário encontrado")
        void deveRetornarDto_quandoUsuarioEncontrado() throws Exception {
            when(repository.findByClerkId("clerk_001")).thenReturn(Optional.of(entity));
            when(adapter.adapterEntityToDto(entity)).thenReturn(dto);

            ContabilUsuarioDto result = service.findByClerkId("clerk_001");

            assertThat(result)
                    .extracting(ContabilUsuarioDto::getClerkId, ContabilUsuarioDto::getEmail)
                    .containsExactly("clerk_001", "joao@email.com");
        }

		@Test
        @DisplayName("Deve lançar NotFoundException quando usuário não encontrado")
        void deveLancarNotFoundException_quandoNaoEncontrado() {
            when(repository.findByClerkId("clerk_999")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findByClerkId("clerk_999"))
                    .isInstanceOf(NotFoundException.class);
        }

		@Test
        @DisplayName("Deve lançar InternalServerErrorException para qualquer outra exceção")
        void deveLancarInternalServerError_quandoExcecaoGenerica() {
            when(repository.findByClerkId(eq("clerk_001")))
                    .thenThrow(new RuntimeException("erro inesperado"));

            assertThatThrownBy(() -> service.findByClerkId("clerk_001"))
                    .isInstanceOf(InternalServerError.class);
        }
	}

	// ─────────────────────────────────────────────────────────────────────────
	// updatePointsAndHearts
	// ─────────────────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("updatePointsAndHearts")
	class UpdatePointsAndHearts {

		@Test
        @DisplayName("Deve retornar 1 quando atualização bem-sucedida")
        void deveRetornar1_quandoAtualizacaoBemSucedida() throws Exception {
            when(repository.updatePointsAndHearts("clerk_001", 3, 200)).thenReturn(1);

            int result = service.updatePointsAndHearts("clerk_001", 3, 200);

            assertThat(result).isEqualTo(1);
            verify(repository, times(1)).updatePointsAndHearts("clerk_001", 3, 200);
        }

		@Test
        @DisplayName("Deve lançar NotFoundException quando nenhum registro atualizado")
        void deveLancarNotFoundException_quandoNenhumRegistroAtualizado() {
            when(repository.updatePointsAndHearts("clerk_999", 3, 200)).thenReturn(0);

            assertThatThrownBy(() -> service.updatePointsAndHearts("clerk_999", 3, 200))
                    .isInstanceOf(NotFoundException.class);
        }

		@Test
        @DisplayName("Deve lançar InternalServerErrorException para qualquer outra exceção")
        void deveLancarInternalServerError_quandoExcecaoGenerica() {
            when(repository.updatePointsAndHearts(any(), anyInt(), anyInt()))
                    .thenThrow(new RuntimeException("erro inesperado"));

            assertThatThrownBy(() -> service.updatePointsAndHearts("clerk_001", 3, 200))
                    .isInstanceOf(InternalServerError.class);
        }
	}

	// ─────────────────────────────────────────────────────────────────────────
	// findTop200ByOrderByPointsDesc
	// ─────────────────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("findTop200ByOrderByPointsDesc")
	class FindTop200 {

		@Test
        @DisplayName("Deve retornar lista paginada com sucesso")
        void deveRetornarListaPaginada() throws Exception {
            when(repository.findTop200ByOrderByPointsDesc(any())).thenReturn(List.of(entity));
            when(adapter.adapterEntityToDto(entity)).thenReturn(dto);

            List<ContabilUsuarioDto> result = service.findTop200ByOrderByPointsDesc(
                    PageRequest.of(0, 200));

            assertThat(result)
                    .hasSize(1)
                    .first()
                    .extracting(ContabilUsuarioDto::getClerkId)
                    .isEqualTo("clerk_001");

            verify(adapter, times(1)).adapterEntityToDto(entity);
        }

		@Test
        @DisplayName("Deve lançar NotFoundException quando lista vazia")
        void deveLancarNotFoundException_quandoListaVazia() {
            when(repository.findTop200ByOrderByPointsDesc(any()))
                    .thenReturn(Collections.emptyList());

            assertThatThrownBy(() -> service.findTop200ByOrderByPointsDesc(PageRequest.of(0, 200)))
                    .isInstanceOf(NotFoundException.class);
        }

		@Test
        @DisplayName("Deve lançar InternalServerErrorException para qualquer outra exceção")
        void deveLancarInternalServerError_quandoExcecaoGenerica() {
            when(repository.findTop200ByOrderByPointsDesc(any()))
                    .thenThrow(new RuntimeException("erro inesperado"));

            assertThatThrownBy(() -> service.findTop200ByOrderByPointsDesc(PageRequest.of(0, 200)))
                    .isInstanceOf(InternalServerError.class);
        }
	}

	// ─────────────────────────────────────────────────────────────────────────
	// findUserRank
	// ─────────────────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("findUserRank")
	class FindUserRank {

		@Test
        @DisplayName("Deve retornar rank do usuário com sucesso")
        void deveRetornarRank() throws Exception {
            when(repository.findUserRank("clerk_001")).thenReturn(Optional.of(3));

            assertThat(service.findUserRank("clerk_001")).isEqualTo(3);
        }

		@Test
        @DisplayName("Deve retornar rank 1 quando usuário é o primeiro colocado")
        void deveRetornarRank1_quandoPrimeiro() throws Exception {
            when(repository.findUserRank("clerk_top")).thenReturn(Optional.of(1));

            assertThat(service.findUserRank("clerk_top")).isEqualTo(1);
        }

		@Test
        @DisplayName("Deve lançar NotFoundException quando rank não encontrado")
        void deveLancarNotFoundException_quandoNaoEncontrado() {
            when(repository.findUserRank("clerk_999")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findUserRank("clerk_999"))
                    .isInstanceOf(NotFoundException.class);
        }

		@Test
        @DisplayName("Deve lançar InternalServerErrorException para qualquer outra exceção")
        void deveLancarInternalServerError_quandoExcecaoGenerica() {
            when(repository.findUserRank(eq("clerk_001")))
                    .thenThrow(new RuntimeException("erro inesperado"));

            assertThatThrownBy(() -> service.findUserRank("clerk_001"))
                    .isInstanceOf(InternalServerError.class);
        }
	}
}