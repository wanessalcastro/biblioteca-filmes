package com.exemplo.bibliotecafilmes.filter;

import com.exemplo.bibliotecafilmes.exception.RateLimitExcedidoException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limitação de taxa por cliente (IP de origem), em janela fixa: no máximo {@code app.rate-limit.requests}
 * requisições a cada {@code app.rate-limit.window-seconds} segundos.
 * <p>Toda resposta traz {@code X-RateLimit-Limit}, {@code X-RateLimit-Remaining} e {@code X-RateLimit-Reset}
 * (segundos até a janela reiniciar). Ao exceder, responde 429 com {@code Retry-After}.
 * O estado fica em memória (instância única), suficiente para este projeto.
 */
@Component
@Order(OrdemFiltros.RATE_LIMIT)
public class RateLimitFilter extends BaseApiFilter {

    private static final class Janela {
        volatile long inicio;
        int contagem;

        Janela(long inicio) {
            this.inicio = inicio;
        }
    }

    private final Map<String, Janela> janelas = new ConcurrentHashMap<>();
    private final int limite;
    private final long janelaMs;

    public RateLimitFilter(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver,
                           @Value("${app.rate-limit.requests:60}") int limite,
                           @Value("${app.rate-limit.window-seconds:60}") long janelaSegundos) {
        super(resolver);
        this.limite = limite;
        this.janelaMs = janelaSegundos * 1000;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long agora = System.currentTimeMillis();
        removerJanelasVencidas(agora);

        // O IP de origem identifica o cliente. Atrás de proxy reverso, configure o servidor para
        // repassar o IP real (server.forward-headers-strategy) — não confiamos em X-Forwarded-For aqui.
        Janela janela = janelas.computeIfAbsent(request.getRemoteAddr(), cliente -> new Janela(agora));

        boolean excedeu;
        int restante;
        long resetSegundos;
        synchronized (janela) {
            if (agora - janela.inicio >= janelaMs) {
                janela.inicio = agora;
                janela.contagem = 0;
            }
            excedeu = janela.contagem >= limite;
            if (!excedeu) {
                janela.contagem++;
            }
            restante = Math.max(0, limite - janela.contagem);
            resetSegundos = Math.max(1, (janela.inicio + janelaMs - agora + 999) / 1000);
        }

        response.setHeader("X-RateLimit-Limit", String.valueOf(limite));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(restante));
        response.setHeader("X-RateLimit-Reset", String.valueOf(resetSegundos));

        if (excedeu) {
            // o cabeçalho Retry-After é adicionado pelo GlobalExceptionHandler
            responderErro(request, response, new RateLimitExcedidoException(limite, resetSegundos));
            return;
        }
        chain.doFilter(request, response);
    }

    /** Evita crescimento indefinido do mapa quando o número de clientes é grande. */
    private void removerJanelasVencidas(long agora) {
        if (janelas.size() > 5_000) {
            janelas.entrySet().removeIf(e -> agora - e.getValue().inicio >= janelaMs);
        }
    }
}
