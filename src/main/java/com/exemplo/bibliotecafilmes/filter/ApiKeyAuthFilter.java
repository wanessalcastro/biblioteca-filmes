package com.exemplo.bibliotecafilmes.filter;

import com.exemplo.bibliotecafilmes.entity.NivelAcesso;
import com.exemplo.bibliotecafilmes.exception.AcessoNegadoException;
import com.exemplo.bibliotecafilmes.exception.ApiKeyInvalidaException;
import com.exemplo.bibliotecafilmes.service.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

/**
 * Autenticação por chave de API (cabeçalho {@code X-API-Key}) com dois níveis de acesso:
 * <ul>
 *   <li>GET/HEAD nos recursos: públicos;</li>
 *   <li>POST/PUT/PATCH: exigem chave válida de nível USUARIO ou ADMIN;</li>
 *   <li>DELETE e qualquer rota de {@code /api-keys}: exigem nível ADMIN.</li>
 * </ul>
 * Sem chave válida → 401; chave válida com nível insuficiente → 403.
 */
@Component
@Order(OrdemFiltros.AUTENTICACAO)
public class ApiKeyAuthFilter extends BaseApiFilter {

    public static final String HEADER = "X-API-Key";
    /** Atributo da requisição com o id da chave autenticada (usado para isolar chaves de idempotência). */
    public static final String ATRIBUTO_CHAVE_ID = ApiKeyAuthFilter.class.getName() + ".chaveId";

    private final ApiKeyService apiKeyService;

    public ApiKeyAuthFilter(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver,
                            ApiKeyService apiKeyService) {
        super(resolver);
        this.apiKeyService = apiKeyService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        NivelAcesso exigido = nivelExigido(request);
        if (exigido != null) {
            try {
                String chave = request.getHeader(HEADER);
                if (chave == null || chave.isBlank()) {
                    throw new ApiKeyInvalidaException("Chave de API ausente. Informe o cabeçalho " + HEADER);
                }
                ApiKeyService.Credencial credencial = apiKeyService.autenticar(chave.trim());
                if (!credencial.nivel().permite(exigido)) {
                    throw new AcessoNegadoException("Esta operação exige nível de acesso " + exigido
                            + " (sua chave tem nível " + credencial.nivel() + ")");
                }
                request.setAttribute(ATRIBUTO_CHAVE_ID, credencial.id());
            } catch (RuntimeException ex) {
                responderErro(request, response, ex);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    /** Nível mínimo exigido pela requisição; {@code null} = rota pública. */
    private NivelAcesso nivelExigido(HttpServletRequest request) {
        if (CaminhosApi.ehRecurso(CaminhosApi.caminho(request), CaminhosApi.API_KEYS)) {
            return NivelAcesso.ADMIN;
        }
        return switch (request.getMethod().toUpperCase()) {
            case "GET", "HEAD" -> null;
            case "DELETE" -> NivelAcesso.ADMIN;
            default -> NivelAcesso.USUARIO;
        };
    }
}
