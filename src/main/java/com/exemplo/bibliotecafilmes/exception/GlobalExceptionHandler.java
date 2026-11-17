package com.exemplo.bibliotecafilmes.exception;

import com.exemplo.bibliotecafilmes.dto.CampoInvalidoDTO;
import com.exemplo.bibliotecafilmes.dto.ErroResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Tratamento global de exceções: toda falha da API (inclusive as lançadas pelos filtros de autenticação,
 * rate limit e idempotência) sai no mesmo formato {@link ErroResponseDTO}, com o status HTTP adequado.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---------- 400 ----------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponseDTO> tratarValidacao(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<CampoInvalidoDTO> campos = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new CampoInvalidoDTO(e.getField(), e.getDefaultMessage()))
                .toList();
        return montar(HttpStatus.BAD_REQUEST, "Dados de entrada inválidos", request, campos, null);
    }

    /** Violação de restrição em parâmetro de rota/consulta (ex.: @NotBlank em @RequestParam). */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErroResponseDTO> tratarValidacaoDeParametros(HandlerMethodValidationException ex,
                                                                      HttpServletRequest request) {
        List<CampoInvalidoDTO> campos = new ArrayList<>();
        for (MessageSourceResolvable erro : ex.getAllErrors()) {
            campos.add(new CampoInvalidoDTO("parametro", erro.getDefaultMessage()));
        }
        return montar(HttpStatus.BAD_REQUEST, "Parâmetros da requisição inválidos", request, campos, null);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErroResponseDTO> tratarRestricoes(ConstraintViolationException ex, HttpServletRequest request) {
        List<CampoInvalidoDTO> campos = new ArrayList<>();
        for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
            campos.add(new CampoInvalidoDTO(v.getPropertyPath().toString(), v.getMessage()));
        }
        return montar(HttpStatus.BAD_REQUEST, "Dados de entrada inválidos", request, campos, null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponseDTO> tratarCorpoIlegivel(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return montar(HttpStatus.BAD_REQUEST,
                "Corpo da requisição ausente ou malformado (verifique o JSON e os formatos de data/enum)", request, null, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponseDTO> tratarTipoInvalido(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return montar(HttpStatus.BAD_REQUEST,
                "Valor inválido para o parâmetro '" + ex.getName() + "'", request, null, null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroResponseDTO> tratarParametroAusente(MissingServletRequestParameterException ex, HttpServletRequest request) {
        return montar(HttpStatus.BAD_REQUEST,
                "Parâmetro obrigatório ausente: '" + ex.getParameterName() + "'", request, null, null);
    }

    @ExceptionHandler(VersaoApiNaoSuportadaException.class)
    public ResponseEntity<ErroResponseDTO> tratarVersaoNaoSuportada(VersaoApiNaoSuportadaException ex, HttpServletRequest request) {
        return montar(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null, null);
    }

    @ExceptionHandler(RequisicaoInvalidaException.class)
    public ResponseEntity<ErroResponseDTO> tratarRequisicaoInvalida(RequisicaoInvalidaException ex, HttpServletRequest request) {
        return montar(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null, null);
    }

    // ---------- 401 / 403 / 429 ----------

    @ExceptionHandler(ApiKeyInvalidaException.class)
    public ResponseEntity<ErroResponseDTO> tratarApiKeyInvalida(ApiKeyInvalidaException ex, HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.WWW_AUTHENTICATE, "ApiKey realm=\"biblioteca-filmes\", header=\"X-API-Key\"");
        return montar(HttpStatus.UNAUTHORIZED, ex.getMessage(), request, null, headers);
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<ErroResponseDTO> tratarAcessoNegado(AcessoNegadoException ex, HttpServletRequest request) {
        return montar(HttpStatus.FORBIDDEN, ex.getMessage(), request, null, null);
    }

    @ExceptionHandler(RateLimitExcedidoException.class)
    public ResponseEntity<ErroResponseDTO> tratarRateLimit(RateLimitExcedidoException ex, HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.RETRY_AFTER, String.valueOf(ex.getRetryAfterSegundos()));
        return montar(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), request, null, headers);
    }

    // ---------- 404 / 405 / 409 / 415 / 422 ----------

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErroResponseDTO> tratarNaoEncontrado(ResourceNotFoundException ex, HttpServletRequest request) {
        return montar(HttpStatus.NOT_FOUND, ex.getMessage(), request, null, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponseDTO> tratarRotaInexistente(NoResourceFoundException ex, HttpServletRequest request) {
        return montar(HttpStatus.NOT_FOUND, "Rota não encontrada: " + request.getRequestURI(), request, null, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponseDTO> tratarMetodoNaoSuportado(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        return montar(HttpStatus.METHOD_NOT_ALLOWED,
                "Método " + ex.getMethod() + " não é permitido nesta rota", request, null, ex.getHeaders());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErroResponseDTO> tratarMediaTypeNaoSuportado(HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        return montar(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Tipo de conteúdo não suportado. Envie o corpo como application/json", request, null, ex.getHeaders());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErroResponseDTO> tratarConflito(ConflictException ex, HttpServletRequest request) {
        return montar(HttpStatus.CONFLICT, ex.getMessage(), request, null, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponseDTO> tratarIntegridade(DataIntegrityViolationException ex, HttpServletRequest request) {
        return montar(HttpStatus.CONFLICT,
                "Operação viola uma restrição de integridade dos dados (registro duplicado ou em uso)", request, null, null);
    }

    @ExceptionHandler(ChaveIdempotenciaReutilizadaException.class)
    public ResponseEntity<ErroResponseDTO> tratarChaveReutilizada(ChaveIdempotenciaReutilizadaException ex, HttpServletRequest request) {
        return montar(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request, null, null);
    }

    // ---------- demais erros ----------

    /**
     * Rede de segurança: exceções do próprio Spring MVC que implementam {@link ErrorResponse} mantêm o status
     * que o framework definiu (ex.: 406); qualquer outra vira 500 sem vazar detalhes internos.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponseDTO> tratarGenerico(Exception ex, HttpServletRequest request) {
        if (ex instanceof ErrorResponse erro) {
            HttpStatusCode codigo = erro.getStatusCode();
            HttpStatus status = HttpStatus.resolve(codigo.value());
            if (status != null) {
                return montar(status, erro.getBody().getDetail() != null ? erro.getBody().getDetail() : status.getReasonPhrase(),
                        request, null, erro.getHeaders());
            }
        }
        log.error("Erro inesperado em {} {}", request.getMethod(), request.getRequestURI(), ex);
        return montar(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno do servidor. Tente novamente mais tarde", request, null, null);
    }

    private ResponseEntity<ErroResponseDTO> montar(HttpStatus status, String mensagem, HttpServletRequest request,
                                                   List<CampoInvalidoDTO> campos, HttpHeaders headers) {
        ErroResponseDTO corpo = new ErroResponseDTO(LocalDateTime.now(), status.value(),
                status.getReasonPhrase(), mensagem, request.getRequestURI(), campos);
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(status);
        if (headers != null) {
            builder.headers(headers);
        }
        return builder.body(corpo);
    }
}
