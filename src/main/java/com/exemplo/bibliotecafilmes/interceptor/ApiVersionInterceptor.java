package com.exemplo.bibliotecafilmes.interceptor;

import com.exemplo.bibliotecafilmes.exception.VersaoApiNaoSuportadaException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

/**
 * Valida o cabeçalho {@code X-API-Version}. Ausente = versão 1 (padrão). Valores fora de {@link #SUPORTADAS}
 * são rejeitados com 400, em vez de cair silenciosamente na versão padrão. A escolha do método de cada versão é
 * feita pelo próprio mapeamento do controller ({@code headers = "X-API-Version=2"}).
 */
public class ApiVersionInterceptor implements HandlerInterceptor {

    public static final String HEADER = "X-API-Version";
    public static final List<String> SUPORTADAS = List.of("1", "2");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String versao = request.getHeader(HEADER);
        if (versao != null && !SUPORTADAS.contains(versao.trim())) {
            throw new VersaoApiNaoSuportadaException(versao, SUPORTADAS);
        }
        return true;
    }
}
