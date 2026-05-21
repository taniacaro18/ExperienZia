package com.experienzia.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Excepción propia del proyecto para errores de negocio.
 * Llevamos el mensaje para el usuario y el código HTTP que queremos devolver.
 */
public class CustomException extends RuntimeException {

	// status = 400, 404, 409, etc. según el caso
	private final HttpStatus status;

	/**
	 * Error con mensaje; por defecto responde 400 BAD_REQUEST.
	 */
	public CustomException(String message) {
		this(message, HttpStatus.BAD_REQUEST);
	}

	/**
	 * Error con mensaje y status HTTP personalizado.
	 */
	public CustomException(String message, HttpStatus status) {
		super(message);
		this.status = status;
	}

	/**
	 * Getter para que GlobalExceptionHandler sepa qué status usar.
	 */
	public HttpStatus getStatus() {
		return status;
	}
}
