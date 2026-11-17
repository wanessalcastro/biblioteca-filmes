package com.exemplo.bibliotecafilmes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para criar ou atualizar um gênero")
public record GeneroRequestDTO(
        @Schema(description = "Nome do gênero (único)", example = "Ficção Científica")
        @NotBlank @Size(min = 2, max = 50) String nome,

        @Schema(description = "Descrição opcional", example = "Filmes baseados em avanços científicos e tecnológicos")
        @Size(max = 255) String descricao) {
}
