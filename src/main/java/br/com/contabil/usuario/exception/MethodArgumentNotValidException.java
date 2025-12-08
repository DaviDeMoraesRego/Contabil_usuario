package br.com.contabil.usuario.exception;

public class MethodArgumentNotValidException extends Exception{

	private static final long serialVersionUID = 1L;

	public MethodArgumentNotValidException(String message) {
		super(message);
	}
}
