package com.exemplo.bibliotecafilmes.controller;

import com.exemplo.bibliotecafilmes.assembler.FilmeModelAssembler;
import com.exemplo.bibliotecafilmes.config.ExemplosOpenApi;
import com.exemplo.bibliotecafilmes.dto.ErroResponseDTO;
import com.exemplo.bibliotecafilmes.dto.FilmeRequestDTO;
import com.exemplo.bibliotecafilmes.dto.FilmeResponseDTO;
import com.exemplo.bibliotecafilmes.dto.FilmeResponseV2DTO;
import com.exemplo.bibliotecafilmes.service.FilmeService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
@RequestMapping("/filmes")
@Tag(name = "Filmes", description = "Cadastro e consulta de filmes")
public class FilmeController {

    private final FilmeService service;
    private final FilmeModelAssembler assembler;

    public FilmeController(FilmeService service, FilmeModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @Operation(summary = "Criar o filme", description = "Cadastra um novo registro e retorna 201 com o header Location.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Filme criado(a); o header Location aponta para o novo recurso",
                    content = @Content(schema = @Schema(implementation = FilmeResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos (lista de campos no corpo)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Dados inválidos", value = ExemplosOpenApi.ERRO_400))),
            @ApiResponse(responseCode = "404", description = "Diretor ou gênero informado não existe",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Não encontrado", value = ExemplosOpenApi.ERRO_404)))
    })
    @PostMapping
    public ResponseEntity<EntityModel<FilmeResponseDTO>> criar(@Valid @RequestBody FilmeRequestDTO dto) {
        EntityModel<FilmeResponseDTO> model = assembler.toModel(service.criar(dto));
        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    @Operation(summary = "Listar filmes", description = "Lista paginada (parâmetros page, size e sort). Retorna PagedModel HAL com links de navegação.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de filmes retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Dados inválidos", value = ExemplosOpenApi.ERRO_400)))
    })
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<FilmeResponseDTO>>> listar(@ParameterObject Pageable pageable,
            @Parameter(hidden = true) org.springframework.data.web.PagedResourcesAssembler<FilmeResponseDTO> pagedAssembler) {
        return ResponseEntity.ok(pagedAssembler.toModel(service.listar(pageable), assembler));
    }

    @Operation(summary = "Buscar o filme por id (versões 1 e 2)",
            description = "Retorna o filme com os links HATEOAS relacionados. **Versionamento por cabeçalho:** "
                    + "sem `X-API-Version` (ou com `X-API-Version: 1`) devolve o formato original; "
                    + "com `X-API-Version: 2` devolve o formato enriquecido (duração formatada, diretor resumido, "
                    + "nomes dos gêneros, média e total de avaliações). A versão servida volta no cabeçalho de resposta `X-API-Version`.",
            parameters = @Parameter(name = "X-API-Version", in = ParameterIn.HEADER, required = false,
                    description = "Versão do contrato desta rota (1 ou 2). Padrão: 1",
                    schema = @Schema(type = "string", allowableValues = {"1", "2"}, defaultValue = "1")))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filme encontrado(a); o formato depende da versão solicitada",
                    headers = @Header(name = "X-API-Version", description = "Versão efetivamente servida", schema = @Schema(type = "string")),
                    content = @Content(mediaType = "application/hal+json",
                            schema = @Schema(oneOf = {FilmeResponseDTO.class, FilmeResponseV2DTO.class}),
                            examples = {
                                    @ExampleObject(name = "Versão 1 (padrão)", value = ExemplosOpenApi.FILME_V1),
                                    @ExampleObject(name = "Versão 2 (X-API-Version: 2)", value = ExemplosOpenApi.FILME_V2)})),
            @ApiResponse(responseCode = "400", description = "Id com formato inválido ou X-API-Version não suportada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Dados inválidos", value = ExemplosOpenApi.ERRO_400))),
            @ApiResponse(responseCode = "404", description = "Filme não encontrado(a)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Não encontrado", value = ExemplosOpenApi.ERRO_404)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<FilmeResponseDTO>> buscarPorId(
            @Parameter(description = "Id do filme", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok().header("X-API-Version", "1").body(assembler.toModel(service.buscarPorId(id)));
    }

    /**
     * Versão 2 da mesma rota {@code GET /filmes/{id}}, selecionada por {@code X-API-Version: 2}.
     * Fica oculta no Swagger porque o springdoc não distingue duas operações de mesmo método e caminho que só
     * diferem por cabeçalho; ela é documentada na operação acima (descrição, parâmetro e exemplos das duas versões).
     */
    @Hidden
    @GetMapping(value = "/{id}", headers = "X-API-Version=2")
    public ResponseEntity<EntityModel<FilmeResponseV2DTO>> buscarPorIdV2(@PathVariable Long id) {
        FilmeResponseV2DTO dto = service.buscarPorIdV2(id);
        EntityModel<FilmeResponseV2DTO> model = EntityModel.of(dto, assembler.links(dto.id(), dto.diretor().id()));
        return ResponseEntity.ok().header("X-API-Version", "2").body(model);
    }

    @Operation(summary = "Atualizar o filme", description = "Substitui os dados do registro identificado pelo id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filme atualizado(a)",
                    content = @Content(schema = @Schema(implementation = FilmeResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Dados inválidos", value = ExemplosOpenApi.ERRO_400))),
            @ApiResponse(responseCode = "404", description = "Filme ou referência informada não encontrado(a)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Não encontrado", value = ExemplosOpenApi.ERRO_404)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<FilmeResponseDTO>> atualizar(
            @Parameter(description = "Id do filme", example = "1") @PathVariable Long id,
            @Valid @RequestBody FilmeRequestDTO dto) {
        return ResponseEntity.ok(assembler.toModel(service.atualizar(id, dto)));
    }

    @Operation(summary = "Remover o filme", description = "Remove o registro identificado pelo id e retorna 204 sem corpo.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Removido com sucesso"),
            @ApiResponse(responseCode = "400", description = "Id com formato inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Dados inválidos", value = ExemplosOpenApi.ERRO_400))),
            @ApiResponse(responseCode = "404", description = "Filme não encontrado(a)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Não encontrado", value = ExemplosOpenApi.ERRO_404)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(
            @Parameter(description = "Id do filme", example = "1") @PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Buscar filmes por título", description = "Retorna, de forma paginada, os filmes cujo título contém o texto informado. Aceita page, size e sort.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de resultados retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetro inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Dados inválidos", value = ExemplosOpenApi.ERRO_400)))
    })
    @GetMapping("/buscar")
    public ResponseEntity<PagedModel<EntityModel<FilmeResponseDTO>>> buscarPorTitulo(
            @Parameter(description = "Trecho do título (sem diferenciar maiúsculas/minúsculas)", example = "inter") @RequestParam @NotBlank(message = "O parâmetro 'titulo' não pode ser vazio") @Size(max = 150, message = "O parâmetro 'titulo' deve ter no máximo 150 caracteres") String titulo,
            @ParameterObject Pageable pageable,
            @Parameter(hidden = true) org.springframework.data.web.PagedResourcesAssembler<FilmeResponseDTO> pagedAssembler) {
        return ResponseEntity.ok(pagedAssembler.toModel(service.buscarPorTitulo(titulo, pageable), assembler));
    }

    @Operation(summary = "Listar filmes por ano de lançamento", description = "Retorna, de forma paginada, os filmes lançados no ano informado. Aceita page, size e sort.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de resultados retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetro inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Dados inválidos", value = ExemplosOpenApi.ERRO_400)))
    })
    @GetMapping("/ano/{ano}")
    public ResponseEntity<PagedModel<EntityModel<FilmeResponseDTO>>> listarPorAno(
            @Parameter(description = "Ano de lançamento", example = "2014") @PathVariable @Min(value = 1888, message = "O ano deve ser maior ou igual a 1888") @Max(value = 2100, message = "O ano deve ser menor ou igual a 2100") Integer ano,
            @ParameterObject Pageable pageable,
            @Parameter(hidden = true) org.springframework.data.web.PagedResourcesAssembler<FilmeResponseDTO> pagedAssembler) {
        return ResponseEntity.ok(pagedAssembler.toModel(service.listarPorAno(ano, pageable), assembler));
    }

    @Operation(summary = "Listar filmes por gênero", description = "Retorna, de forma paginada, os filmes do gênero informado. Aceita page, size e sort.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de resultados retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetro inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Dados inválidos", value = ExemplosOpenApi.ERRO_400))),
            @ApiResponse(responseCode = "404", description = "Gênero não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Não encontrado", value = ExemplosOpenApi.ERRO_404)))
    })
    @GetMapping("/genero/{generoId}")
    public ResponseEntity<PagedModel<EntityModel<FilmeResponseDTO>>> listarPorGenero(
            @Parameter(description = "Id do gênero", example = "1") @PathVariable Long generoId,
            @ParameterObject Pageable pageable,
            @Parameter(hidden = true) org.springframework.data.web.PagedResourcesAssembler<FilmeResponseDTO> pagedAssembler) {
        return ResponseEntity.ok(pagedAssembler.toModel(service.listarPorGenero(generoId, pageable), assembler));
    }

    @Operation(summary = "Listar filmes por diretor", description = "Retorna, de forma paginada, os filmes dirigidos pelo diretor informado. Aceita page, size e sort.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de resultados retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetro inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Dados inválidos", value = ExemplosOpenApi.ERRO_400))),
            @ApiResponse(responseCode = "404", description = "Diretor não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDTO.class),
                            examples = @ExampleObject(name = "Não encontrado", value = ExemplosOpenApi.ERRO_404)))
    })
    @GetMapping("/diretor/{diretorId}")
    public ResponseEntity<PagedModel<EntityModel<FilmeResponseDTO>>> listarPorDiretor(
            @Parameter(description = "Id do diretor", example = "1") @PathVariable Long diretorId,
            @ParameterObject Pageable pageable,
            @Parameter(hidden = true) org.springframework.data.web.PagedResourcesAssembler<FilmeResponseDTO> pagedAssembler) {
        return ResponseEntity.ok(pagedAssembler.toModel(service.listarPorDiretor(diretorId, pageable), assembler));
    }

}
