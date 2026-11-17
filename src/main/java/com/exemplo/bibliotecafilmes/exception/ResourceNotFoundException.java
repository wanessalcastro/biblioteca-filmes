package com.exemplo.bibliotecafilmes.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String recurso, Long id) {
        super(recurso + " não encontrado(a) com id " + id);
    }

    public ResourceNotFoundException(String mensagem) {
        super(mensagem);
    }
}
