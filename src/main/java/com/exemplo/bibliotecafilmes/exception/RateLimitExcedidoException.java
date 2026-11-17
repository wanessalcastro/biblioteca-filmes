package com.exemplo.bibliotecafilmes.exception;

/** Limite de requisições excedido — mapeada para HTTP 429 com o cabeçalho Retry-After. */
public class RateLimitExcedidoException extends RuntimeException {

    private final long retryAfterSegundos;

    public RateLimitExcedidoException(int limite, long retryAfterSegundos) {
        super("Limite de " + limite + " requisições por janela excedido. Tente novamente em "
                + retryAfterSegundos + " segundo(s)");
        this.retryAfterSegundos = retryAfterSegundos;
    }

    public long getRetryAfterSegundos() {
        return retryAfterSegundos;
    }
}
