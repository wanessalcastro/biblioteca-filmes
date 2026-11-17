package com.exemplo.bibliotecafilmes.controller;

import com.exemplo.bibliotecafilmes.assembler.DiretorModelAssembler;
import com.exemplo.bibliotecafilmes.config.ExemplosOpenApi;
import com.exemplo.bibliotecafilmes.dto.ErroResponseDTO;
import com.exemplo.bibliotecafilmes.dto.DiretorRequestDTO;
import com.exemplo.bibliotecafilmes.dto.DiretorResponseDTO;
import com.exemplo.bibliotecafilmes.service.DiretorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/diretores")
@Tag(name = "Diretores", description = "Cadastro e consulta de diretores")
public class DiretorController {

    private final DiretorService service;
    private final DiretorModelAssembler assembler;

    public DiretorController(DiretorService service, DiretorModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @Operation(summary = "Criar o diretor", description = "Cadastra um novo registro e retorna 201 com o header Location.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Diretor criado(a); o header Location aponta para o novo recurso",
                    content = @Content(schema = @Schema(implementation = DiretorResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos (lista de campos no corpo)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Dados inválidos", value = ExemplosOpenApi.ERRO_400)))
    })
    @PostMapping
    public ResponseEntity<EntityModel<DiretorResponseDTO>> criar(@Valid @RequestBody DiretorRequestDTO dto) {
        EntityModel<DiretorResponseDTO> model = assembler.toModel(service.criar(dto));
        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    @Operation(summary = "Listar diretores", description = "Lista paginada (parâmetros page, size e sort). Retorna PagedModel HAL com links de navegação.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de diretores retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Dados inválidos", value = ExemplosOpenApi.ERRO_400)))
    })
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<DiretorResponseDTO>>> listar(@ParameterObject Pageable pageable,
            @Parameter(hidden = true) org.springframework.data.web.PagedResourcesAssembler<DiretorResponseDTO> pagedAssembler) {
        return ResponseEntity.ok(pagedAssembler.toModel(service.listar(pageable), assembler));
    }

    @Operation(summary = "Buscar o diretor por id", description = "Retorna o diretor com os links HATEOAS relacionados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Diretor encontrado(a)",
                    content = @Content(schema = @Schema(implementation = DiretorResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Id com formato inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Dados inválidos", value = ExemplosOpenApi.ERRO_400))),
            @ApiResponse(responseCode = "404", description = "Diretor não encontrado(a)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Não encontrado", value = ExemplosOpenApi.ERRO_404)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<DiretorResponseDTO>> buscarPorId(
            @Parameter(description = "Id do diretor", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(assembler.toModel(service.buscarPorId(id)));
    }

    @Operation(summary = "Atualizar o diretor", description = "Substitui os dados do registro identificado pelo id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Diretor atualizado(a)",
                    content = @Content(schema = @Schema(implementation = DiretorResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Dados inválidos", value = ExemplosOpenApi.ERRO_400))),
            @ApiResponse(responseCode = "404", description = "Diretor não encontrado(a)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Não encontrado", value = ExemplosOpenApi.ERRO_404)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<DiretorResponseDTO>> atualizar(
            @Parameter(description = "Id do diretor", example = "1") @PathVariable Long id,
            @Valid @RequestBody DiretorRequestDTO dto) {
        return ResponseEntity.ok(assembler.toModel(service.atualizar(id, dto)));
    }

    @Operation(summary = "Remover o diretor", description = "Remove o registro identificado pelo id e retorna 204 sem corpo.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Removido com sucesso"),
            @ApiResponse(responseCode = "400", description = "Id com formato inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Dados inválidos", value = ExemplosOpenApi.ERRO_400))),
            @ApiResponse(responseCode = "404", description = "Diretor não encontrado(a)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Não encontrado", value = ExemplosOpenApi.ERRO_404))),
            @ApiResponse(responseCode = "409", description = "Conflito: existem filmes associados ao diretor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Conflito", value = ExemplosOpenApi.ERRO_409)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(
            @Parameter(description = "Id do diretor", example = "1") @PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Listar diretores por nacionalidade", description = "Retorna, de forma paginada, os diretores da nacionalidade informada. Aceita page, size e sort.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de resultados retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetro inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Dados inválidos", value = ExemplosOpenApi.ERRO_400)))
    })
    @GetMapping("/nacionalidade/{nacionalidade}")
    public ResponseEntity<PagedModel<EntityModel<DiretorResponseDTO>>> listarPorNacionalidade(
            @Parameter(description = "Nacionalidade (sem diferenciar maiúsculas/minúsculas)", example = "Britânico") @PathVariable @NotBlank(message = "A nacionalidade não pode ser vazia") @Size(max = 60, message = "A nacionalidade deve ter no máximo 60 caracteres") String nacionalidade,
            @ParameterObject Pageable pageable,
            @Parameter(hidden = true) org.springframework.data.web.PagedResourcesAssembler<DiretorResponseDTO> pagedAssembler) {
        return ResponseEntity.ok(pagedAssembler.toModel(service.listarPorNacionalidade(nacionalidade, pageable), assembler));
    }

}
