package com.exemplo.bibliotecafilmes.exception;

/** Chave válida, porém com nível de acesso insuficiente — mapeada para HTTP 403. */
public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
