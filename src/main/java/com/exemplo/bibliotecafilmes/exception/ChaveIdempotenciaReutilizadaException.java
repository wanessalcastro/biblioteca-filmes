package com.exemplo.bibliotecafilmes.exception;

/** Mesma X-Idempotency-Key usada com um corpo/rota diferente — mapeada para HTTP 422. */
public class ChaveIdempotenciaReutilizadaException extends RuntimeException {

    public ChaveIdempotenciaReutilizadaException(String mensagem) {
        super(mensagem);
    }
}
