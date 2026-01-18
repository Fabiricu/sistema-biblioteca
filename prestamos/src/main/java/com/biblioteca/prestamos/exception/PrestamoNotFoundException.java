package com.biblioteca.prestamos.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PrestamoNotFoundException extends RuntimeException {

    public PrestamoNotFoundException(Long id) {
        super(String.format("Préstamo con ID %d no encontrado", id));
    }
}
