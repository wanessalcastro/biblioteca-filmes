package com.exemplo.bibliotecafilmes.exception;

/** Requisição malformada por regra de negócio/cabeçalho — mapeada para HTTP 400. */
public class RequisicaoInvalidaException extends RuntimeException {

    public RequisicaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}
