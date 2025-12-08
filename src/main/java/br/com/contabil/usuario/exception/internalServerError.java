package br.com.contabil.usuario.exception;

public class internalServerError extends Exception {

	private static final long serialVersionUID = 1L;

	public internalServerError(String message) {
		super(message);
	}
}
