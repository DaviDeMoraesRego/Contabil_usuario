package br.com.contabil.usuario.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDto<T> implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private T data;
	private String errors;
}
