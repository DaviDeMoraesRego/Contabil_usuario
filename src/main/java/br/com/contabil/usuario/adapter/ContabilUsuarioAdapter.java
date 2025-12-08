package br.com.contabil.usuario.adapter;

import org.springframework.stereotype.Component;

import br.com.contabil.usuario.dto.ContabilUsuarioDto;
import br.com.contabil.usuario.entity.ContabilUsuarioEntity;

@Component
public class ContabilUsuarioAdapter {

	public ContabilUsuarioEntity adapterDtoToEntity (ContabilUsuarioDto dto, String clerkId) {
		
		ContabilUsuarioEntity entity = new ContabilUsuarioEntity();
		
		entity.setClerkId(clerkId);
		entity.setNome(dto.getNome());
		entity.setEmail(dto.getEmail());
		entity.setUserImgSrc(dto.getUserImgSrc());
		entity.setActiveCourse(dto.getActiveCourse());
		entity.setHearts(dto.getHearts());
		entity.setPoints(dto.getPoints());
		
		return entity;
	}
	
	public ContabilUsuarioDto adapterEntityToDto (ContabilUsuarioEntity entity) {
		
		ContabilUsuarioDto dto = new ContabilUsuarioDto();
		
		dto.setClerkId(entity.getClerkId());
		dto.setNome(entity.getNome());
		dto.setEmail(entity.getEmail());
		dto.setUserImgSrc(entity.getUserImgSrc());
		dto.setActiveCourse(entity.getActiveCourse());
		dto.setHearts(entity.getHearts());
		dto.setPoints(entity.getPoints());
		
		return dto;
	}
}
