package br.com.contabil.usuario.dto;

import java.io.Serializable;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ContabilUsuarioDto implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@NotBlank(message = "insira um clerk ID válido.")
	private String clerkId;
	
	private String nome;
	
	@NotBlank(message = "insira um email válido.")
	private String email;
	
	private String userImgSrc;
	
	private int activeCourse;
	
	@Min(message = "Número de corações inválido.", value = 0)
	private int hearts;
	
	@Min(message = "Número de corações inválido.", value = 0)
	private int points;
	
	
}
