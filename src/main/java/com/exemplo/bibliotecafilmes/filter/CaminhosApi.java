package com.exemplo.bibliotecafilmes.filter;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/** Identifica as rotas de negócio da API (Swagger, console H2 e estáticos ficam fora dos filtros). */
public final class CaminhosApi {

    public static final String API_KEYS = "/api-keys";

    private static final List<String> RECURSOS = List.of(
            "/filmes", "/diretores", "/generos", "/usuarios", "/avaliacoes", API_KEYS);

    private CaminhosApi() {
    }

    /** Caminho da requisição sem o context-path. */
    public static String caminho(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contexto = request.getContextPath();
        return (contexto != null && !contexto.isEmpty() && uri.startsWith(contexto))
                ? uri.substring(contexto.length()) : uri;
    }

    /** {@code true} se o caminho é exatamente o prefixo ou está abaixo dele (ex.: /filmes e /filmes/1). */
    public static boolean ehRecurso(String caminho, String prefixo) {
        return caminho.equals(prefixo) || caminho.startsWith(prefixo + "/");
    }

    public static boolean ehRecursoDaApi(HttpServletRequest request) {
        String caminho = caminho(request);
        return RECURSOS.stream().anyMatch(prefixo -> ehRecurso(caminho, prefixo));
    }
}
