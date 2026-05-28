package com.experienzia.exceptions;

import org.springframework.http.HttpStatus;

// Yo lanzo esto cuando el negocio no cuadra y quiero mandar error con status claro al front
public class CustomException extends RuntimeException {

	private final HttpStatus status;

	public CustomException(String message) {
		this(message, HttpStatus.BAD_REQUEST);
	}

	public CustomException(String message, HttpStatus status) {
		super(message);
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}
}
