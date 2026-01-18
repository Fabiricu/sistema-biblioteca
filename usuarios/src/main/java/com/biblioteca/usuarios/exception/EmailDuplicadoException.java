// EmailDuplicadoException.java
package com.biblioteca.usuarios.exception;

public class EmailDuplicadoException extends RuntimeException {
    public EmailDuplicadoException(String message) {
        super(message);
    }
}