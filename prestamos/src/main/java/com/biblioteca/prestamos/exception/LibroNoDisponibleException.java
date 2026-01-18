package com.biblioteca.prestamos.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class LibroNoDisponibleException extends RuntimeException {

    public LibroNoDisponibleException(Long libroId) {
        super(String.format("El libro con ID %d no está disponible para préstamo", libroId));
    }
}
