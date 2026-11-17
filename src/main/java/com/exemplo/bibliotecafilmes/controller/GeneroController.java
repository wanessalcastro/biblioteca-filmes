package com.exemplo.bibliotecafilmes.controller;

import com.exemplo.bibliotecafilmes.assembler.GeneroModelAssembler;
import com.exemplo.bibliotecafilmes.config.ExemplosOpenApi;
import com.exemplo.bibliotecafilmes.dto.ErroResponseDTO;
import com.exemplo.bibliotecafilmes.dto.GeneroRequestDTO;
import com.exemplo.bibliotecafilmes.dto.GeneroResponseDTO;
import com.exemplo.bibliotecafilmes.service.GeneroService;
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
@RequestMapping("/generos")
@Tag(name = "Gêneros", description = "Cadastro e consulta de gêneros de filmes")
public class GeneroController {

    private final GeneroService service;
    private final GeneroModelAssembler assembler;

    public GeneroController(GeneroService service, GeneroModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @Operation(summary = "Criar o gênero", description = "Cadastra um novo registro e retorna 201 com o header Location.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Genero criado(a); o header Location aponta para o novo recurso",
                    content = @Content(schema = @Schema(implementation = GeneroResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos (lista de campos no corpo)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Dados inválidos", value = ExemplosOpenApi.ERRO_400))),
            @ApiResponse(responseCode = "409", description = "Conflito: já existe um gênero com o mesmo nome",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Conflito", value = ExemplosOpenApi.ERRO_409)))
    })
    @PostMapping
    public ResponseEntity<EntityModel<GeneroResponseDTO>> criar(@Valid @RequestBody GeneroRequestDTO dto) {
        EntityModel<GeneroResponseDTO> model = assembler.toModel(service.criar(dto));
        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    @Operation(summary = "Listar gêneros", description = "Lista paginada (parâmetros page, size e sort). Retorna PagedModel HAL com links de navegação.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de gêneros retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Dados inválidos", value = ExemplosOpenApi.ERRO_400)))
    })
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<GeneroResponseDTO>>> listar(@ParameterObject Pageable pageable,
            @Parameter(hidden = true) org.springframework.data.web.PagedResourcesAssembler<GeneroResponseDTO> pagedAssembler) {
        return ResponseEntity.ok(pagedAssembler.toModel(service.listar(pageable), assembler));
    }

    @Operation(summary = "Buscar o gênero por id", description = "Retorna o gênero com os links HATEOAS relacionados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Genero encontrado(a)",
                    content = @Content(schema = @Schema(implementation = GeneroResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Id com formato inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Dados inválidos", value = ExemplosOpenApi.ERRO_400))),
            @ApiResponse(responseCode = "404", description = "Genero não encontrado(a)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Não encontrado", value = ExemplosOpenApi.ERRO_404)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<GeneroResponseDTO>> buscarPorId(
            @Parameter(description = "Id do gênero", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(assembler.toModel(service.buscarPorId(id)));
    }

    @Operation(summary = "Atualizar o gênero", description = "Substitui os dados do registro identificado pelo id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Genero atualizado(a)",
                    content = @Content(schema = @Schema(implementation = GeneroResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Dados inválidos", value = ExemplosOpenApi.ERRO_400))),
            @ApiResponse(responseCode = "404", description = "Genero não encontrado(a)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Não encontrado", value = ExemplosOpenApi.ERRO_404))),
            @ApiResponse(responseCode = "409", description = "Conflito: já existe um gênero com o mesmo nome",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Conflito", value = ExemplosOpenApi.ERRO_409)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<GeneroResponseDTO>> atualizar(
            @Parameter(description = "Id do gênero", example = "1") @PathVariable Long id,
            @Valid @RequestBody GeneroRequestDTO dto) {
        return ResponseEntity.ok(assembler.toModel(service.atualizar(id, dto)));
    }

    @Operation(summary = "Remover o gênero", description = "Remove o registro identificado pelo id e retorna 204 sem corpo.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Removido com sucesso"),
            @ApiResponse(responseCode = "400", description = "Id com formato inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Dados inválidos", value = ExemplosOpenApi.ERRO_400))),
            @ApiResponse(responseCode = "404", description = "Genero não encontrado(a)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Não encontrado", value = ExemplosOpenApi.ERRO_404))),
            @ApiResponse(responseCode = "409", description = "Conflito: existem filmes associados ao gênero",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Conflito", value = ExemplosOpenApi.ERRO_409)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(
            @Parameter(description = "Id do gênero", example = "1") @PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Listar gêneros de um filme", description = "Retorna, de forma paginada, os gêneros associados ao filme informado. Aceita page, size e sort.")
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
    public ResponseEntity<PagedModel<EntityModel<GeneroResponseDTO>>> listarPorFilme(
            @Parameter(description = "Id do filme", example = "1") @PathVariable Long filmeId,
            @ParameterObject Pageable pageable,
            @Parameter(hidden = true) org.springframework.data.web.PagedResourcesAssembler<GeneroResponseDTO> pagedAssembler) {
        return ResponseEntity.ok(pagedAssembler.toModel(service.listarPorFilme(filmeId, pageable), assembler));
    }

}
