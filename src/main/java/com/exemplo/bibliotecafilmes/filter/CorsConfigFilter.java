package com.exemplo.bibliotecafilmes.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * Política de CORS da API. Roda como filtro (e não via WebMvcConfigurer) para que também as respostas de erro
 * geradas por outros filtros — 401, 403, 429 — carreguem os cabeçalhos CORS e possam ser lidas pelo navegador.
 * Origens, métodos e cabeçalhos vêm de {@code app.cors.*} no application.properties.
 */
@Component
@Order(OrdemFiltros.CORS)
public class CorsConfigFilter extends CorsFilter {

    /** Cabeçalhos que o JavaScript do navegador pode ler nas respostas. */
    private static final List<String> CABECALHOS_EXPOSTOS = List.of(
            "Location", "Retry-After", "X-RateLimit-Limit", "X-RateLimit-Remaining", "X-RateLimit-Reset",
            "X-API-Version", "Idempotent-Replayed");

    public CorsConfigFilter(@Value("${app.cors.allowed-origins}") String origens,
                            @Value("${app.cors.allowed-methods}") String metodos,
                            @Value("${app.cors.allowed-headers}") String cabecalhos,
                            @Value("${app.cors.max-age-seconds:3600}") long maxAge) {
        super(fonte(origens, metodos, cabecalhos, maxAge));
    }

    private static CorsConfigurationSource fonte(String origens, String metodos, String cabecalhos, long maxAge) {
        CorsConfiguration configuracao = new CorsConfiguration();
        configuracao.setAllowedOrigins(dividir(origens));
        configuracao.setAllowedMethods(dividir(metodos));
        configuracao.setAllowedHeaders(dividir(cabecalhos));
        configuracao.setExposedHeaders(CABECALHOS_EXPOSTOS);
        configuracao.setAllowCredentials(false); // autenticação é por cabeçalho X-API-Key, não por cookie
        configuracao.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
        fonte.registerCorsConfiguration("/**", configuracao);
        return fonte;
    }

    private static List<String> dividir(String valor) {
        return Arrays.stream(valor.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }
}
