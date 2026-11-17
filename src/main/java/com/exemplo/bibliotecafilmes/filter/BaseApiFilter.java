package com.exemplo.bibliotecafilmes.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

/**
 * Base dos filtros da API. Exceções lançadas em filtros não chegam ao {@code @RestControllerAdvice} sozinhas;
 * por isso elas são encaminhadas ao {@link HandlerExceptionResolver} do MVC, que aciona o
 * GlobalExceptionHandler e mantém o mesmo corpo de erro (ErroResponseDTO) em toda a API.
 */
public abstract class BaseApiFilter extends OncePerRequestFilter {

    private final HandlerExceptionResolver resolver;

    protected BaseApiFilter(HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    /** Só filtra as rotas da API e ignora o preflight CORS (OPTIONS), tratado pelo filtro de CORS. */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod()) || !CaminhosApi.ehRecursoDaApi(request);
    }

    protected void responderErro(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        ModelAndView resolvido = resolver.resolveException(request, response, null, ex);
        if (resolvido == null) {
            throw new IllegalStateException("Erro não tratado pelo GlobalExceptionHandler", ex);
        }
    }
}
