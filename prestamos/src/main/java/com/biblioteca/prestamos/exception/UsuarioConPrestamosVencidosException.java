package com.biblioteca.prestamos.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UsuarioConPrestamosVencidosException extends RuntimeException {

    public UsuarioConPrestamosVencidosException(Long usuarioId) {
        super(String.format("El usuario con ID %d tiene préstamos vencidos", usuarioId));
    }
}
