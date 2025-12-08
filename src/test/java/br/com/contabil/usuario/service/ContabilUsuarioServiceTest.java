package br.com.contabil.usuario.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.contabil.usuario.adapter.ContabilUsuarioAdapter;
import br.com.contabil.usuario.dto.ContabilUsuarioDto;
import br.com.contabil.usuario.entity.ContabilUsuarioEntity;
import br.com.contabil.usuario.exception.BadRequestException;
import br.com.contabil.usuario.exception.NotFoundExceptionException;
import br.com.contabil.usuario.exception.internalServerError;
import br.com.contabil.usuario.repository.ContabilUsuarioRepository;

@ExtendWith(MockitoExtension.class)
public class ContabilUsuarioServiceTest {

	@Mock
	ContabilUsuarioRepository repository;

	@Mock
	ContabilUsuarioAdapter adapter;

	@InjectMocks
	ContabilUsuarioService service;

	@Nested
	@DisplayName("Testes do CreateOrUpdate")
	class WhenCreateOrUpdate {

		@Test
		@DisplayName("Quando chamar createOrUpdate, deve retornar o clerkId salvo.")
		void whenCallCreateOrUpdate_thenReturnClerkId() throws Exception {

			ContabilUsuarioDto dto = new ContabilUsuarioDto();
			dto.setClerkId("123");

			ContabilUsuarioEntity entity = new ContabilUsuarioEntity();
			entity.setClerkId("123");

			when(adapter.adapterDtoToEntity(any(), eq("123"))).thenReturn(entity);
			when(repository.save(any(ContabilUsuarioEntity.class))).thenReturn(entity);

			String actual = service.createOrUpdate(dto);

			assertEquals("123", actual);
			verify(repository, times(1)).save(any(ContabilUsuarioEntity.class));
		}

		@Test
		@DisplayName("Quando chamar createOrUpdate, deve lançar BadRequestException.")
		void whenCallCreateOrUpdate_thenThrowBadRequest() throws Exception {

			ContabilUsuarioDto dto = new ContabilUsuarioDto();
			dto.setClerkId("123");

			when(adapter.adapterDtoToEntity(any(), eq("123"))).thenThrow(new IllegalArgumentException("erro"));

			assertThrows(BadRequestException.class, () -> service.createOrUpdate(dto));
		}

		@Test
		@DisplayName("Quando chamar createOrUpdate, deve lançar internalServerError.")
		void whenCallCreateOrUpdate_thenThrowInternalServerError() throws Exception {

			ContabilUsuarioDto dto = new ContabilUsuarioDto();
			dto.setClerkId("123");

			when(adapter.adapterDtoToEntity(any(), eq("123"))).thenThrow(new RuntimeException("falha inesperada"));

			assertThrows(internalServerError.class, () -> service.createOrUpdate(dto));
		}
	}

	@Nested
	@DisplayName("Testes do FindAll")
	class WhenFindAll {

		@Test
		@DisplayName("Quando chamar findAll, deve retornar lista de DTOs")
		void whenCallFindAll_thenReturnDtoList() throws Exception {

			ContabilUsuarioEntity entity = new ContabilUsuarioEntity();
			ContabilUsuarioDto dto = new ContabilUsuarioDto();

			when(repository.findAll()).thenReturn(List.of(entity));
			when(adapter.adapterEntityToDto(entity)).thenReturn(dto);

			List<ContabilUsuarioDto> actual = service.findAll();

			assertEquals(1, actual.size());
			verify(repository, times(1)).findAll();
		}

		@Test
        @DisplayName("Quando findAll não encontrar registros, deve lançar NotFoundExceptionException")
        void whenCallFindAll_thenThrowNotFound() throws Exception {

            when(repository.findAll()).thenReturn(List.of());

            assertThrows(NotFoundExceptionException.class, () -> service.findAll());
        }

		@Test
        @DisplayName("Quando findAll lançar erro inesperado, deve lançar internalServerError")
        void whenCallFindAll_thenThrowInternal() throws Exception {

            when(repository.findAll()).thenThrow(RuntimeException.class);

            assertThrows(internalServerError.class, () -> service.findAll());
        }
	}

	@Nested
	@DisplayName("Testes do FindByClerkId")
	class WhenFindByClerkId {

		@Test
		@DisplayName("Quando chamar findByClerkId, deve retornar dto correto.")
		void whenCallFindByClerkId_thenReturnDto() throws Exception {

			String clerkId = "ABC";

			ContabilUsuarioEntity entity = new ContabilUsuarioEntity();
			ContabilUsuarioDto dto = new ContabilUsuarioDto();

			when(repository.findByClerkId(clerkId)).thenReturn(Optional.of(entity));
			when(adapter.adapterEntityToDto(entity)).thenReturn(dto);

			ContabilUsuarioDto actual = service.findByClerkId(clerkId);

			assertEquals(dto, actual);
		}

		@Test
        @DisplayName("Quando findByClerkId não encontrar usuário, deve lançar NotFound")
        void whenCallFindByClerkId_thenThrowNotFound() throws Exception {

            when(repository.findByClerkId("X")).thenReturn(Optional.empty());

            assertThrows(NotFoundExceptionException.class, () -> service.findByClerkId("X"));
        }

		@Test
        @DisplayName("Quando findByClerkId lançar erro inesperado, deve lançar internalServerError")
        void whenCallFindByClerkId_thenThrowInternal() throws Exception {

            when(repository.findByClerkId("X")).thenThrow(RuntimeException.class);

            assertThrows(internalServerError.class, () -> service.findByClerkId("X"));
        }
	}

	@Nested
	@DisplayName("Testes do UpdatePointsAndHearts")
	class WhenUpdatePointsAndHearts {

		@Test
        @DisplayName("Quando updatePointsAndHearts atualizar com sucesso, deve retornar quantidade atualizada.")
        void whenCallUpdatePoints_thenReturnUpdated() throws Exception {

            when(repository.updatePointsAndHearts("A", 3, 4)).thenReturn(1);

            int actual = service.updatePointsAndHearts("A", 3, 4);

            assertEquals(1, actual);
        }

		@Test
        @DisplayName("Quando updatePointsAndHearts retornar  0, deve lançar NotFound")
        void whenCallUpdatePoints_thenThrowNotFound() throws Exception {

            when(repository.updatePointsAndHearts("A", 3, 4)).thenReturn(0);

            assertThrows(NotFoundExceptionException.class,
                    () -> service.updatePointsAndHearts("A", 3, 4));
        }

		@Test
        @DisplayName("Quando updatePointsAndHearts lançar erro inesperado, deve lançar internalServerError")
        void whenCallUpdatePoints_thenThrowInternal() throws Exception {

            when(repository.updatePointsAndHearts("A", 3, 4)).thenThrow(RuntimeException.class);

            assertThrows(internalServerError.class,
                    () -> service.updatePointsAndHearts("A", 3, 4));
        }
	}
}
