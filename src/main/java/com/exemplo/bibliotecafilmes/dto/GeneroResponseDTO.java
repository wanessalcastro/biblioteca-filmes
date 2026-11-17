package com.exemplo.bibliotecafilmes.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Gênero retornado pela API")
public record GeneroResponseDTO(
        @Schema(description = "Identificador", example = "1") Long id,
        @Schema(description = "Nome do gênero", example = "Ficção Científica") String nome,
        @Schema(description = "Descrição", example = "Filmes baseados em avanços científicos e tecnológicos") String descricao) {
}
