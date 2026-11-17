package com.exemplo.bibliotecafilmes.controller;

import com.exemplo.bibliotecafilmes.assembler.AvaliacaoModelAssembler;
import com.exemplo.bibliotecafilmes.config.ExemplosOpenApi;
import com.exemplo.bibliotecafilmes.dto.ErroResponseDTO;
import com.exemplo.bibliotecafilmes.dto.AvaliacaoRequestDTO;
import com.exemplo.bibliotecafilmes.dto.AvaliacaoResponseDTO;
import com.exemplo.bibliotecafilmes.service.AvaliacaoService;
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

@RestController
@RequestMapping("/avaliacoes")
@Tag(name = "Avaliações", description = "Avaliações de filmes feitas por usuários")
public class AvaliacaoController {

    private final AvaliacaoService service;
    private final AvaliacaoModelAssembler assembler;

    public AvaliacaoController(AvaliacaoService service, AvaliacaoModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @Operation(summary = "Criar a avaliação", description = "Cadastra um novo registro e retorna 201 com o header Location.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Avaliacao criado(a); o header Location aponta para o novo recurso",
                    content = @Content(schema = @Schema(implementation = AvaliacaoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos (lista de campos no corpo)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Dados inválidos", value = ExemplosOpenApi.ERRO_400))),
            @ApiResponse(responseCode = "404", description = "Filme ou usuário informado não existe",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Não encontrado", value = ExemplosOpenApi.ERRO_404))),
            @ApiResponse(responseCode = "409", description = "Conflito: o usuário já avaliou este filme",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Conflito", value = ExemplosOpenApi.ERRO_409)))
    })
    @PostMapping
    public ResponseEntity<EntityModel<AvaliacaoResponseDTO>> criar(@Valid @RequestBody AvaliacaoRequestDTO dto) {
        EntityModel<AvaliacaoResponseDTO> model = assembler.toModel(service.criar(dto));
        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    @Operation(summary = "Listar avaliações", description = "Lista paginada (parâmetros page, size e sort). Retorna PagedModel HAL com links de navegação.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de avaliações retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Dados inválidos", value = ExemplosOpenApi.ERRO_400)))
    })
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<AvaliacaoResponseDTO>>> listar(@ParameterObject Pageable pageable,
            @Parameter(hidden = true) org.springframework.data.web.PagedResourcesAssembler<AvaliacaoResponseDTO> pagedAssembler) {
        return ResponseEntity.ok(pagedAssembler.toModel(service.listar(pageable), assembler));
    }

    @Operation(summary = "Buscar a avaliação por id", description = "Retorna a avaliação com os links HATEOAS relacionados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Avaliacao encontrado(a)",
                    content = @Content(schema = @Schema(implementation = AvaliacaoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Id com formato inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Dados inválidos", value = ExemplosOpenApi.ERRO_400))),
            @ApiResponse(responseCode = "404", description = "Avaliacao não encontrado(a)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Não encontrado", value = ExemplosOpenApi.ERRO_404)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<AvaliacaoResponseDTO>> buscarPorId(
            @Parameter(description = "Id da avaliação", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(assembler.toModel(service.buscarPorId(id)));
    }

    @Operation(summary = "Atualizar a avaliação", description = "Substitui os dados do registro identificado pelo id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Avaliacao atualizado(a)",
                    content = @Content(schema = @Schema(implementation = AvaliacaoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Dados inválidos", value = ExemplosOpenApi.ERRO_400))),
            @ApiResponse(responseCode = "404", description = "Avaliacao ou referência informada não encontrado(a)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Não encontrado", value = ExemplosOpenApi.ERRO_404))),
            @ApiResponse(responseCode = "409", description = "Conflito: o usuário já avaliou este filme",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Conflito", value = ExemplosOpenApi.ERRO_409)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<AvaliacaoResponseDTO>> atualizar(
            @Parameter(description = "Id da avaliação", example = "1") @PathVariable Long id,
            @Valid @RequestBody AvaliacaoRequestDTO dto) {
        return ResponseEntity.ok(assembler.toModel(service.atualizar(id, dto)));
    }

    @Operation(summary = "Remover a avaliação", description = "Remove o registro identificado pelo id e retorna 204 sem corpo.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Removido com sucesso"),
            @ApiResponse(responseCode = "400", description = "Id com formato inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Dados inválidos", value = ExemplosOpenApi.ERRO_400))),
            @ApiResponse(responseCode = "404", description = "Avaliacao não encontrado(a)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Não encontrado", value = ExemplosOpenApi.ERRO_404)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(
            @Parameter(description = "Id da avaliação", example = "1") @PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Listar avaliações de um filme", description = "Retorna, de forma paginada, as avaliações do filme informado. Aceita page, size e sort.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de resultados retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetro inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Dados inválidos", value = ExemplosOpenApi.ERRO_400))),
            @ApiResponse(responseCode = "404", description = "Filme não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Não encontrado", value = ExemplosOpenApi.ERRO_404)))
    })
    @GetMapping("/filme/{filmeId}")
    public ResponseEntity<PagedModel<EntityModel<AvaliacaoResponseDTO>>> listarPorFilme(
            @Parameter(description = "Id do filme", example = "1") @PathVariable Long filmeId,
            @ParameterObject Pageable pageable,
            @Parameter(hidden = true) org.springframework.data.web.PagedResourcesAssembler<AvaliacaoResponseDTO> pagedAssembler) {
        return ResponseEntity.ok(pagedAssembler.toModel(service.listarPorFilme(filmeId, pageable), assembler));
    }

    @Operation(summary = "Listar avaliações de um usuário", description = "Retorna, de forma paginada, as avaliações feitas pelo usuário informado. Aceita page, size e sort.")
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
    public ResponseEntity<PagedModel<EntityModel<AvaliacaoResponseDTO>>> listarPorUsuario(
            @Parameter(description = "Id do usuário", example = "1") @PathVariable Long usuarioId,
            @ParameterObject Pageable pageable,
            @Parameter(hidden = true) org.springframework.data.web.PagedResourcesAssembler<AvaliacaoResponseDTO> pagedAssembler) {
        return ResponseEntity.ok(pagedAssembler.toModel(service.listarPorUsuario(usuarioId, pageable), assembler));
    }

}
