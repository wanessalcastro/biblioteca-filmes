package com.exemplo.bibliotecafilmes.exception;

/** Conflito de regra de negócio (ex.: e-mail duplicado) — mapeado para HTTP 409. */
public class ConflictException extends RuntimeException {

    public ConflictException(String mensagem) {
        super(mensagem);
    }
}
