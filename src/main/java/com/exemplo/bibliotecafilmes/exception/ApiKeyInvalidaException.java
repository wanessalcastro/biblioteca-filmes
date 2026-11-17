package com.exemplo.bibliotecafilmes.exception;

/** Chave de API ausente, inválida, revogada ou expirada — mapeada para HTTP 401. */
public class ApiKeyInvalidaException extends RuntimeException {

    public ApiKeyInvalidaException(String mensagem) {
        super(mensagem);
    }
}
