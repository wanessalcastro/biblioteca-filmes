package com.exemplo.bibliotecafilmes.filter;

/**
 * Ordem de execução dos filtros (menor = mais externo). Todos rodam depois do RequestContextFilter (-105)
 * do Spring Boot, de modo que o tratamento de exceções funciona normalmente.
 * CORS → rate limit → autenticação por API Key → idempotência.
 */
public final class OrdemFiltros {

    private OrdemFiltros() {
    }

    public static final int CORS = -104;
    public static final int RATE_LIMIT = -103;
    public static final int AUTENTICACAO = -102;
    public static final int IDEMPOTENCIA = -101;
}
