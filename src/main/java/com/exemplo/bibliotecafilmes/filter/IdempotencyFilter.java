package com.exemplo.bibliotecafilmes.filter;

import com.exemplo.bibliotecafilmes.entity.IdempotencyRecord;
import com.exemplo.bibliotecafilmes.exception.ChaveIdempotenciaReutilizadaException;
import com.exemplo.bibliotecafilmes.exception.ConflictException;
import com.exemplo.bibliotecafilmes.exception.RequisicaoInvalidaException;
import com.exemplo.bibliotecafilmes.service.IdempotenciaService;
import com.exemplo.bibliotecafilmes.util.HashUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Idempotência para POST via cabeçalho {@code X-Idempotency-Key}.
 * <ol>
 *   <li>1ª requisição com a chave: é processada normalmente e, se der certo (2xx), a resposta é guardada;</li>
 *   <li>repetição com a mesma chave e o mesmo conteúdo: a resposta guardada é devolvida sem reprocessar
 *       (nada é criado de novo), com o cabeçalho {@code Idempotent-Replayed: true};</li>
 *   <li>mesma chave com outro corpo/rota: 422;</li>
 *   <li>mesma chave enquanto a 1ª ainda está em andamento: 409.</li>
 * </ol>
 * O cabeçalho é opcional (sem ele o POST segue o fluxo normal). As chaves são isoladas por chave de API, então
 * clientes diferentes podem usar o mesmo valor sem colidir. Erros (4xx/5xx) não são guardados, permitindo
 * corrigir o problema e repetir com a mesma chave. {@code /api-keys} fica de fora para que o segredo gerado
 * nunca seja gravado no banco.
 */
@Component
@Order(OrdemFiltros.IDEMPOTENCIA)
public class IdempotencyFilter extends BaseApiFilter {

    public static final String HEADER = "X-Idempotency-Key";
    public static final String HEADER_REPLAY = "Idempotent-Replayed";

    private static final int TAMANHO_MAXIMO_CHAVE = 128;
    private static final int TAMANHO_MAXIMO_CORPO = 1_048_576; // 1 MB

    private final IdempotenciaService servico;
    private final Set<String> emAndamento = ConcurrentHashMap.newKeySet();

    public IdempotencyFilter(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver,
                             IdempotenciaService servico) {
        super(resolver);
        this.servico = servico;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return super.shouldNotFilter(request)
                || !"POST".equalsIgnoreCase(request.getMethod())
                || request.getHeader(HEADER) == null
                || CaminhosApi.ehRecurso(CaminhosApi.caminho(request), CaminhosApi.API_KEYS);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String chaveCliente = request.getHeader(HEADER).trim();
        if (chaveCliente.isEmpty() || chaveCliente.length() > TAMANHO_MAXIMO_CHAVE) {
            responderErro(request, response, new RequisicaoInvalidaException(
                    "O cabeçalho " + HEADER + " deve ter entre 1 e " + TAMANHO_MAXIMO_CHAVE + " caracteres"));
            return;
        }

        byte[] corpo = request.getInputStream().readNBytes(TAMANHO_MAXIMO_CORPO + 1);
        if (corpo.length > TAMANHO_MAXIMO_CORPO) {
            responderErro(request, response, new RequisicaoInvalidaException(
                    "Corpo maior que 1 MB não é suportado em requisições com " + HEADER));
            return;
        }

        Object chaveApiId = request.getAttribute(ApiKeyAuthFilter.ATRIBUTO_CHAVE_ID);
        String escopo = (chaveApiId == null ? "anonimo" : chaveApiId) + ":" + chaveCliente;
        String impressao = HashUtil.sha256Hex(
                HashUtil.sha256Hex(request.getMethod() + " " + request.getRequestURI() + "?" + request.getQueryString())
                        + HashUtil.sha256Hex(corpo));

        if (responderSeJaProcessada(escopo, impressao, request, response)) {
            return;
        }
        if (!emAndamento.add(escopo)) {
            responderErro(request, response, new ConflictException(
                    "Já existe uma requisição em processamento com esta " + HEADER + ". Aguarde e tente novamente"));
            return;
        }

        ContentCachingResponseWrapper resposta = new ContentCachingResponseWrapper(response);
        try {
            // revalida: outra requisição pode ter terminado entre a primeira consulta e o registro em andamento
            if (responderSeJaProcessada(escopo, impressao, request, response)) {
                return;
            }
            resposta.setHeader(HEADER_REPLAY, "false");
            chain.doFilter(new CachedBodyRequest(request, corpo), resposta);

            int status = resposta.getStatus();
            if (status >= 200 && status < 300) {
                guardar(escopo, impressao, resposta);
            }
        } finally {
            emAndamento.remove(escopo);
            resposta.copyBodyToResponse();
        }
    }

    /** Se a chave já tem resposta guardada, reproduz (ou rejeita, se o conteúdo mudou) e devolve {@code true}. */
    private boolean responderSeJaProcessada(String escopo, String impressao,
                                            HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Optional<IdempotencyRecord> existente = servico.buscarValido(escopo);
        if (existente.isEmpty()) {
            return false;
        }
        IdempotencyRecord registro = existente.get();
        if (!registro.getImpressaoDigital().equals(impressao)) {
            responderErro(request, response, new ChaveIdempotenciaReutilizadaException(
                    "A " + HEADER + " informada já foi usada em outra requisição com conteúdo diferente"));
            return true;
        }

        byte[] corpo = registro.getCorpo() == null ? new byte[0] : registro.getCorpo().getBytes(StandardCharsets.UTF_8);
        response.setStatus(registro.getStatusHttp());
        if (registro.getContentType() != null) {
            response.setContentType(registro.getContentType());
        }
        if (registro.getLocation() != null) {
            response.setHeader("Location", registro.getLocation());
        }
        response.setHeader(HEADER_REPLAY, "true");
        response.setContentLength(corpo.length);
        response.getOutputStream().write(corpo);
        return true;
    }

    private void guardar(String escopo, String impressao, ContentCachingResponseWrapper resposta) {
        IdempotencyRecord registro = new IdempotencyRecord(escopo, impressao, resposta.getStatus(),
                resposta.getContentType(), resposta.getHeader("Location"),
                new String(resposta.getContentAsByteArray(), StandardCharsets.UTF_8));
        try {
            servico.salvar(registro);
        } catch (DataIntegrityViolationException ignorada) {
            // outra instância gravou a mesma chave no mesmo instante: o resultado dela é o que vale
        }
    }
}
