package com.exemplo.bibliotecafilmes.controller;

import com.exemplo.bibliotecafilmes.assembler.ApiKeyModelAssembler;
import com.exemplo.bibliotecafilmes.config.ExemplosOpenApi;
import com.exemplo.bibliotecafilmes.dto.ApiKeyCriadaResponseDTO;
import com.exemplo.bibliotecafilmes.dto.ApiKeyRequestDTO;
import com.exemplo.bibliotecafilmes.dto.ApiKeyResponseDTO;
import com.exemplo.bibliotecafilmes.dto.ApiKeyUpdateDTO;
import com.exemplo.bibliotecafilmes.dto.ErroResponseDTO;
import com.exemplo.bibliotecafilmes.service.ApiKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Geração e gerenciamento de chaves de API. Todas as rotas exigem uma chave de nível ADMIN
 * (cabeçalho X-API-Key) — a regra é aplicada pelo ApiKeyAuthFilter.
 */
@RestController
@RequestMapping("/api-keys")
@Tag(name = "Chaves de API", description = "Geração e gerenciamento de chaves de API (exige chave de nível ADMIN)")
public class ApiKeyController {

    private final ApiKeyService service;
    private final ApiKeyModelAssembler assembler;

    public ApiKeyController(ApiKeyService service, ApiKeyModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @Operation(summary = "Gerar chave de API",
            description = "Gera uma chave para um usuário e retorna 201 com o header Location. "
                    + "**O valor secreto (`chave`) é exibido somente nesta resposta**; no banco fica apenas o hash.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Chave gerada; o header Location aponta para o recurso",
                    content = @Content(schema = @Schema(implementation = ApiKeyCriadaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos (lista de campos no corpo)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Dados inválidos", value = ExemplosOpenApi.ERRO_400))),
            @ApiResponse(responseCode = "404", description = "Usuário informado não existe",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Não encontrado", value = ExemplosOpenApi.ERRO_404)))
    })
    @PostMapping
    public ResponseEntity<EntityModel<ApiKeyCriadaResponseDTO>> criar(@Valid @RequestBody ApiKeyRequestDTO dto) {
        ApiKeyCriadaResponseDTO criada = service.criar(dto);
        EntityModel<ApiKeyCriadaResponseDTO> model =
                EntityModel.of(criada, assembler.links(criada.id(), criada.usuarioId()));
        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    @Operation(summary = "Listar chaves de API", description = "Lista paginada (parâmetros page, size e sort). Nunca inclui o valor secreto.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de chaves retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Dados inválidos", value = ExemplosOpenApi.ERRO_400)))
    })
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<ApiKeyResponseDTO>>> listar(@ParameterObject Pageable pageable,
            @Parameter(hidden = true) org.springframework.data.web.PagedResourcesAssembler<ApiKeyResponseDTO> pagedAssembler) {
        return ResponseEntity.ok(pagedAssembler.toModel(service.listar(pageable), assembler));
    }

    @Operation(summary = "Buscar chave de API por id", description = "Retorna os metadados da chave (sem o valor secreto) e seus links HATEOAS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Chave encontrada",
                    content = @Content(schema = @Schema(implementation = ApiKeyResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Id com formato inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Dados inválidos", value = ExemplosOpenApi.ERRO_400))),
            @ApiResponse(responseCode = "404", description = "Chave não encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Não encontrado", value = ExemplosOpenApi.ERRO_404)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<ApiKeyResponseDTO>> buscarPorId(
            @Parameter(description = "Id da chave", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(assembler.toModel(service.buscarPorId(id)));
    }

    @Operation(summary = "Atualizar chave de API",
            description = "Altera descrição, nível, situação (ativa) e validade. O valor secreto não muda; para trocá-lo, gere uma nova chave.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Chave atualizada",
                    content = @Content(schema = @Schema(implementation = ApiKeyResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Dados inválidos", value = ExemplosOpenApi.ERRO_400))),
            @ApiResponse(responseCode = "404", description = "Chave não encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Não encontrado", value = ExemplosOpenApi.ERRO_404)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<ApiKeyResponseDTO>> atualizar(
            @Parameter(description = "Id da chave", example = "1") @PathVariable Long id,
            @Valid @RequestBody ApiKeyUpdateDTO dto) {
        return ResponseEntity.ok(assembler.toModel(service.atualizar(id, dto)));
    }

    @Operation(summary = "Revogar chave de API",
            description = "Revoga a chave (ativa=false) e retorna 204 sem corpo. O registro é mantido para histórico e a chave deixa de autenticar imediatamente.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Chave revogada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Id com formato inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Dados inválidos", value = ExemplosOpenApi.ERRO_400))),
            @ApiResponse(responseCode = "404", description = "Chave não encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Não encontrado", value = ExemplosOpenApi.ERRO_404)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revogar(
            @Parameter(description = "Id da chave", example = "1") @PathVariable Long id) {
        service.revogar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Listar chaves de um usuário", description = "Retorna, de forma paginada, as chaves do usuário informado. Aceita page, size e sort.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de resultados retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetro inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Dados inválidos", value = ExemplosOpenApi.ERRO_400))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Não encontrado", value = ExemplosOpenApi.ERRO_404)))
    })
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<PagedModel<EntityModel<ApiKeyResponseDTO>>> listarPorUsuario(
            @Parameter(description = "Id do usuário", example = "1") @PathVariable Long usuarioId,
            @ParameterObject Pageable pageable,
            @Parameter(hidden = true) org.springframework.data.web.PagedResourcesAssembler<ApiKeyResponseDTO> pagedAssembler) {
        return ResponseEntity.ok(pagedAssembler.toModel(service.listarPorUsuario(usuarioId, pageable), assembler));
    }
}
