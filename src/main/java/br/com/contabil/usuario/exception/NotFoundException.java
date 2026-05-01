package br.com.contabil.usuario.exception;

public class NotFoundExceptionException extends Exception {

	private static final long serialVersionUID = 1L;

	public NotFoundExceptionException(String message) {
		super(message);
	}
}
