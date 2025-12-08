package br.com.contabil.usuario.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import br.com.contabil.usuario.adapter.ContabilUsuarioAdapter;
import br.com.contabil.usuario.dto.ContabilUsuarioDto;
import br.com.contabil.usuario.entity.ContabilUsuarioEntity;
import br.com.contabil.usuario.exception.BadRequestException;
import br.com.contabil.usuario.exception.NotFoundExceptionException;
import br.com.contabil.usuario.exception.internalServerError;
import br.com.contabil.usuario.repository.ContabilUsuarioRepository;

@Service
public class ContabilUsuarioService {

	@Autowired
	ContabilUsuarioRepository repository;

	@Autowired
	ContabilUsuarioAdapter adapter;

	public String createOrUpdate(ContabilUsuarioDto dto) throws Exception {
		String clerkId = null;

		try {
			ContabilUsuarioEntity entity = adapter.adapterDtoToEntity(dto, dto.getClerkId());
			clerkId = repository.save(entity).getClerkId();
		} catch (IllegalArgumentException | OptimisticLockingFailureException e) {
			throw new BadRequestException(e.getMessage());
		} catch (Exception e) {
			throw new internalServerError(e.getMessage());
		}
		return clerkId;
	}

	public List<ContabilUsuarioDto> findAll() throws Exception {
		List<ContabilUsuarioDto> dtos = new ArrayList<>();
		try {
			List<ContabilUsuarioEntity> entities = repository.findAll();
			notFoundChecker(entities.size());
			entities.forEach(entity -> dtos.add(adapter.adapterEntityToDto(entity)));
		} catch (NotFoundExceptionException e) {
			throw new NotFoundExceptionException(e.getMessage());
		} catch (Exception e) {
			throw new internalServerError(e.getMessage());
		}
		return dtos;
	}

	public ContabilUsuarioDto findByClerkId(String clerkId) throws Exception {
		ContabilUsuarioDto dto;
		try {
			ContabilUsuarioEntity entity = repository.findByClerkId(clerkId)
					.orElseThrow(() -> new NotFoundExceptionException("Nenhum registro encontrado."));
			dto = adapter.adapterEntityToDto(entity);
		} catch (NotFoundExceptionException e) {
			throw new NotFoundExceptionException(e.getMessage());
		} catch (Exception e) {
			throw new internalServerError(e.getMessage());
		}
		return dto;
	}

	public int updatePointsAndHearts(String clerkId, int hearts, int points) throws Exception {
		int updated = 0;
		try {
			updated = repository.updatePointsAndHearts(clerkId, hearts, points);
			if (updated == 0) {
				throw new NotFoundExceptionException("Usuário não encontrado para o clerkId: " + clerkId);
			}
		} catch (NotFoundExceptionException e) {
			throw new NotFoundExceptionException(e.getMessage());
		} catch (Exception e) {
			throw new internalServerError(e.getMessage());
		}
		return updated;
	}

	private static void notFoundChecker(int paramForCheck) throws NotFoundExceptionException {
		if (paramForCheck == 0) {
			throw new NotFoundExceptionException("Nenhum registro encontrado.");
		}
	}
}
