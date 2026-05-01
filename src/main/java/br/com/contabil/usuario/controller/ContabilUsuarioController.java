package br.com.contabil.usuario.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.contabil.usuario.dto.ContabilUsuarioDto;
import br.com.contabil.usuario.dto.ResponseDto;
import br.com.contabil.usuario.service.ContabilUsuarioService;
import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/contabil-usuario/v1")
public class ContabilUsuarioController {

	@Autowired
	ContabilUsuarioService service;

	@PostMapping
	public ResponseEntity<ResponseDto<String>> create(@RequestBody @Valid ContabilUsuarioDto dto) throws Exception {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(new ResponseDto<String>(service.createOrUpdate(dto), null));
	}

	@GetMapping
	public ResponseEntity<ResponseDto<List<ContabilUsuarioDto>>> findAll() throws Exception {
		return ResponseEntity.ok(new ResponseDto<List<ContabilUsuarioDto>>(service.findAll(), null));
	}

	@GetMapping("/{clerkId}/")
	public ResponseEntity<ResponseDto<ContabilUsuarioDto>> findByClerkId(@PathVariable("clerkId") String clerkId)
			throws Exception {
		return ResponseEntity.ok(new ResponseDto<ContabilUsuarioDto>(service.findByClerkId(clerkId), null));
	}

	@PutMapping("/{clerkId}/{hearts}/{points}")
	public ResponseEntity<ResponseDto<Integer>> update(@PathVariable("clerkId") String clerkId,
			@PathVariable("hearts") int hearts, @PathVariable("points") int points) throws Exception {
		return ResponseEntity
				.ok(new ResponseDto<Integer>(service.updatePointsAndHearts(clerkId, hearts, points), null));
	}

	@GetMapping("/ranking/")
	public ResponseEntity<ResponseDto<List<ContabilUsuarioDto>>> findTop200ByOrderByPointsDesc() throws Exception {
		return ResponseEntity.ok(new ResponseDto<List<ContabilUsuarioDto>>(
				service.findTop200ByOrderByPointsDesc(PageRequest.of(0, 200, Sort.by("points").descending())), null));
	}

	@GetMapping("/rank/{clerkId}/")
	public ResponseEntity<ResponseDto<Integer>> findUserRank(@PathVariable("clerkId") String clerkId) throws Exception {
		return ResponseEntity.ok(new ResponseDto<Integer>(service.findUserRank(clerkId), null));
	}
}