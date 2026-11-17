package com.exemplo.bibliotecafilmes.config;

import com.exemplo.bibliotecafilmes.controller.ApiKeyController;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class OpenApiConfig {

    public static final String ESQUEMA_API_KEY = "ApiKeyAuth";

    @Bean
    public OpenAPI bibliotecaFilmesOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Biblioteca de Filmes")
                        .version("2.0.0")
                        .description("API RESTful para gerenciar filmes, diretores, gêneros, usuários e avaliações, "
                                + "com paginação, Bean Validation e links HATEOAS.\n\n"
                        .contact(new Contact()
                                .email("wanessacastro206@gmail.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://mit-license.org/")))
                .components(new Components().addSecuritySchemes(ESQUEMA_API_KEY, new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-API-Key")
                        .description("Chave de API gerada em `POST /api-keys`. Chaves de demonstração estão no README.")));
    }

    /**
     * Acrescenta a todas as operações o que é transversal e, por isso, não está nos controllers:
     * segurança e respostas 401/403 nas rotas protegidas, cabeçalho X-Idempotency-Key nos POST e resposta 429.
     */
    @Bean
    public OperationCustomizer recursosAvancadosCustomizer() {
        return (Operation operation, org.springframework.web.method.HandlerMethod handlerMethod) -> {
            boolean escrita = handlerMethod.hasMethodAnnotation(PostMapping.class)
                    || handlerMethod.hasMethodAnnotation(PutMapping.class)
                    || handlerMethod.hasMethodAnnotation(PatchMapping.class)
                    || handlerMethod.hasMethodAnnotation(DeleteMapping.class);
            boolean gerenciamentoDeChaves = ApiKeyController.class.isAssignableFrom(handlerMethod.getBeanType());

            if (escrita || gerenciamentoDeChaves) {
                operation.addSecurityItem(new SecurityRequirement().addList(ESQUEMA_API_KEY));
                adicionarResposta(operation, "401",
                        "Chave de API ausente, inválida, revogada ou expirada (cabeçalho X-API-Key)",
                        erro(401, "Unauthorized", "Chave de API ausente. Informe o cabeçalho X-API-Key"));
                adicionarResposta(operation, "403",
                        "Chave válida, mas sem nível de acesso suficiente (DELETE e /api-keys exigem ADMIN)",
                        erro(403, "Forbidden", "Esta operação exige nível de acesso ADMIN (sua chave tem nível USUARIO)"));
            }

            if (handlerMethod.hasMethodAnnotation(PostMapping.class) && !gerenciamentoDeChaves) {
                operation.addParametersItem(new HeaderParameter()
                        .name("X-Idempotency-Key")
                        .required(false)
                        .description("Chave de idempotência (até 128 caracteres, ex.: um UUID). Repetir o POST com a mesma "
                                + "chave e o mesmo corpo devolve a resposta original, com `Idempotent-Replayed: true`, "
                                + "sem criar um novo registro. Mesma chave com corpo diferente → 422; "
                                + "mesma chave com a 1ª requisição ainda em andamento → 409.")
                        .schema(new StringSchema().example("7b2f6c1e-3a54-4d0e-9a6e-1c2d3e4f5a6b")));
                adicionarResposta(operation, "422",
                        "X-Idempotency-Key já utilizada em uma requisição com conteúdo diferente",
                        erro(422, "Unprocessable Entity",
                                "A X-Idempotency-Key informada já foi usada em outra requisição com conteúdo diferente"));
            }

            ApiResponse limite = adicionarResposta(operation, "429",
                    "Limite de requisições excedido; aguarde o tempo indicado em Retry-After",
                    erro(429, "Too Many Requests",
                            "Limite de 60 requisições por janela excedido. Tente novamente em 42 segundo(s)"));
            if (limite != null) {
                limite.addHeaderObject("Retry-After", new Header()
                        .description("Segundos até uma nova tentativa ser aceita").schema(new IntegerSchema().example(42)));
                limite.addHeaderObject("X-RateLimit-Limit", new Header()
                        .description("Máximo de requisições por janela").schema(new IntegerSchema().example(60)));
                limite.addHeaderObject("X-RateLimit-Remaining", new Header()
                        .description("Requisições restantes na janela atual").schema(new IntegerSchema().example(0)));
            }
            return operation;
        };
    }

    /** Adiciona a resposta, se a operação ainda não a documentou; devolve-a (ou null se já existia). */
    private ApiResponse adicionarResposta(Operation operation, String codigo, String descricao, Map<String, Object> exemplo) {
        ApiResponses respostas = operation.getResponses();
        if (respostas == null) {
            respostas = new ApiResponses();
            operation.setResponses(respostas);
        }
        if (respostas.containsKey(codigo)) {
            return null;
        }
        Schema<Object> schema = new Schema<>();
        schema.set$ref("#/components/schemas/ErroResponseDTO");
        MediaType json = new MediaType().schema(schema).addExamples("Exemplo", new Example().value(exemplo));
        ApiResponse resposta = new ApiResponse().description(descricao)
                .content(new Content().addMediaType("application/json", json));
        respostas.addApiResponse(codigo, resposta);
        return resposta;
    }

    private Map<String, Object> erro(int status, String erro, String mensagem) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("timestamp", "2026-11-17T10:30:00");
        corpo.put("status", status);
        corpo.put("erro", erro);
        corpo.put("mensagem", mensagem);
        corpo.put("caminho", "/recursos");
        corpo.put("campos", null);
        return corpo;
    }
}
