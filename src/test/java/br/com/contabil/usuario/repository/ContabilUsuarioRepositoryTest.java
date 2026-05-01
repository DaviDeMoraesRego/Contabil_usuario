package br.com.contabil.usuario.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import br.com.contabil.usuario.entity.ContabilUsuarioEntity;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("ContabilUsuarioRepository - Testes de Integração")
class ContabilUsuarioRepositoryTest {

	@Autowired
	private ContabilUsuarioRepository repository;

	// ─────────────────────────────────────────────────────────────────────────
	// Test Data Builder
	// ─────────────────────────────────────────────────────────────────────────

	private ContabilUsuarioEntity buildUsuario(String clerkId, String nome, String email, int activeCourse, int hearts,
			int points) {
		ContabilUsuarioEntity u = new ContabilUsuarioEntity();
		u.setClerkId(clerkId);
		u.setNome(nome);
		u.setEmail(email);
		u.setActiveCourse(activeCourse);
		u.setHearts(hearts);
		u.setPoints(points);
		return u;
	}

	@BeforeEach
	void setUp() {
		repository.deleteAll();
		repository.saveAll(List.of(buildUsuario("clerk_001", "João", "joao@email.com", 1, 5, 300),
				buildUsuario("clerk_002", "Maria", "maria@email.com", 2, 3, 500),
				buildUsuario("clerk_003", "Carlos", "carlos@email.com", 1, 4, 100)));
	}

	// ─────────────────────────────────────────────────────────────────────────
	// findByClerkId
	// ─────────────────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("findByClerkId")
	class FindByClerkId {

		@Test
		@DisplayName("Deve retornar o usuário correto pelo clerkId")
		void deveRetornarUsuario_quandoEncontrado() {
			Optional<ContabilUsuarioEntity> result = repository.findByClerkId("clerk_001");

			assertThat(result).isPresent().hasValueSatisfying(u -> {
				assertThat(u.getClerkId()).isEqualTo("clerk_001");
				assertThat(u.getNome()).isEqualTo("João");
				assertThat(u.getEmail()).isEqualTo("joao@email.com");
			});
		}

		@Test
		@DisplayName("Deve retornar Optional vazio para clerkId inexistente")
		void deveRetornarVazio_quandoNaoEncontrado() {
			assertThat(repository.findByClerkId("clerk_999")).isEmpty();
		}
	}

	// ─────────────────────────────────────────────────────────────────────────
	// deleteByClerkId
	// ─────────────────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("deleteByClerkId")
	class DeleteByClerkId {

		@Test
		@DisplayName("Deve remover o usuário corretamente")
		void deveRemoverUsuario() {
			repository.deleteByClerkId("clerk_001");

			assertThat(repository.findByClerkId("clerk_001")).isEmpty();
		}

		@Test
		@DisplayName("Não deve afetar outros usuários ao deletar")
		void naoDeveAfetarOutrosUsuarios() {
			repository.deleteByClerkId("clerk_001");

			assertThat(repository.findAll()).hasSize(2);
			assertThat(repository.findByClerkId("clerk_002")).isPresent();
			assertThat(repository.findByClerkId("clerk_003")).isPresent();
		}

		@Test
		@DisplayName("Não deve lançar exceção ao deletar clerkId inexistente")
		void naoDeveLancarExcecao_quandoClerkIdInexistente() {
			assertThatCode(() -> repository.deleteByClerkId("clerk_999")).doesNotThrowAnyException();
		}
	}

	// ─────────────────────────────────────────────────────────────────────────
	// updatePointsAndHearts
	// ─────────────────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("updatePointsAndHearts")
	class UpdatePointsAndHearts {

		@Test
		@DisplayName("Deve atualizar points e hearts corretamente")
		void deveAtualizarCampos() {
			int updated = repository.updatePointsAndHearts("clerk_001", 10, 999);

			assertThat(updated).isEqualTo(1);

			assertThat(repository.findByClerkId("clerk_001")).isPresent().hasValueSatisfying(u -> {
				assertThat(u.getHearts()).isEqualTo(10);
				assertThat(u.getPoints()).isEqualTo(999);
			});
		}

		@Test
		@DisplayName("Não deve alterar outros campos ao atualizar points e hearts")
		void naoDeveAlterarOutrosCampos() {
			repository.updatePointsAndHearts("clerk_001", 10, 999);

			assertThat(repository.findByClerkId("clerk_001")).isPresent().hasValueSatisfying(u -> {
				assertThat(u.getNome()).isEqualTo("João");
				assertThat(u.getEmail()).isEqualTo("joao@email.com");
				assertThat(u.getActiveCourse()).isEqualTo(1);
			});
		}

		@Test
		@DisplayName("Deve retornar 0 para clerkId inexistente")
		void deveRetornarZero_quandoNaoEncontrado() {
			assertThat(repository.updatePointsAndHearts("clerk_999", 10, 999)).isZero();
		}

		@Test
		@DisplayName("Não deve afetar outros usuários ao atualizar")
		void naoDeveAfetarOutrosUsuarios() {
			repository.updatePointsAndHearts("clerk_001", 10, 999);

			assertThat(repository.findByClerkId("clerk_002")).isPresent().hasValueSatisfying(u -> {
				assertThat(u.getHearts()).isEqualTo(3);
				assertThat(u.getPoints()).isEqualTo(500);
			});
		}
	}

	// ─────────────────────────────────────────────────────────────────────────
	// findTop200ByOrderByPointsDesc
	// ─────────────────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("findTop200ByOrderByPointsDesc")
	class FindTop200 {

		@Test
		@DisplayName("Deve retornar usuários ordenados por pontos decrescente")
		void deveRetornarOrdenadoPorPontosDesc() {
			List<ContabilUsuarioEntity> result = repository.findTop200ByOrderByPointsDesc(PageRequest.of(0, 200));

			assertThat(result).hasSize(3).extracting(ContabilUsuarioEntity::getClerkId).containsExactly("clerk_002",
					"clerk_001", "clerk_003");
		}

		@Test
		@DisplayName("Deve respeitar o limite de paginação")
		void deveRespeitarLimiteDePaginacao() {
			List<ContabilUsuarioEntity> result = repository.findTop200ByOrderByPointsDesc(PageRequest.of(0, 1));

			assertThat(result).hasSize(1).first().extracting(ContabilUsuarioEntity::getClerkId).isEqualTo("clerk_002");
		}

		@Test
		@DisplayName("Deve retornar lista vazia quando não há usuários")
		void deveRetornarListaVazia_quandoSemUsuarios() {
			repository.deleteAll();

			assertThat(repository.findTop200ByOrderByPointsDesc(PageRequest.of(0, 200))).isEmpty();
		}
	}

	// ─────────────────────────────────────────────────────────────────────────
	// findUserRank
	// ─────────────────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("findUserRank")
	class FindUserRank {

		@Test
		@DisplayName("Deve retornar rank 1 para o usuário com mais pontos")
		void deveRetornarRank1_quandoMaioresPontos() {
			assertThat(repository.findUserRank("clerk_002")).isPresent().hasValue(1);
		}

		@Test
		@DisplayName("Deve retornar rank correto para usuário intermediário")
		void deveRetornarRankCorreto_quandoIntermediario() {
			assertThat(repository.findUserRank("clerk_001")).isPresent().hasValue(2);
		}

		@Test
		@DisplayName("Deve retornar rank correto para o último colocado")
		void deveRetornarRankCorreto_quandoUltimoColocado() {
			assertThat(repository.findUserRank("clerk_003")).isPresent().hasValue(3);
		}

		@Test
		@DisplayName("Deve atualizar rank corretamente após mudança de pontos")
		void deveAtualizarRank_aposAlteracaoDePontos() {
			repository.updatePointsAndHearts("clerk_003", 4, 9999);

			assertThat(repository.findUserRank("clerk_003")).isPresent().hasValue(1);
		}
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Callbacks JPA
	// ─────────────────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("Callbacks JPA")
	class CallbacksJpa {

		@Test
		@DisplayName("Deve preencher dthr_create e dthr_update automaticamente ao persistir")
		void devePreencherDatasAoPersistir() {
			ContabilUsuarioEntity novo = buildUsuario("clerk_new", null, "novo@email.com", 0, 0, 0);

			ContabilUsuarioEntity salvo = repository.save(novo);

			assertThat(salvo.getDthr_create()).isNotNull();
			assertThat(salvo.getDthr_update()).isNotNull();
		}

		@Test
		@DisplayName("Deve atualizar dthr_update ao fazer update sem alterar dthr_create")
		void deveAtualizarDthrUpdate_semAlterarDthrCreate() {
			ContabilUsuarioEntity entity = repository.findByClerkId("clerk_001").orElseThrow();
			Date dthrCreateOriginal = entity.getDthr_create();

			// Avança dthr_update manualmente — sem Thread.sleep
			entity.setNome("João Atualizado");
			entity.setDthr_update(new Date(dthrCreateOriginal.getTime() + 1000));
			ContabilUsuarioEntity atualizado = repository.saveAndFlush(entity);

			assertThat(atualizado.getDthr_create()).isEqualTo(dthrCreateOriginal);
			assertThat(atualizado.getDthr_update()).isAfter(dthrCreateOriginal);
		}
	}
}