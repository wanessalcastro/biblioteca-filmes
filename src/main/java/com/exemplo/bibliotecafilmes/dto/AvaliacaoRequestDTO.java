package com.exemplo.bibliotecafilmes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para criar ou atualizar uma avaliação")
public record AvaliacaoRequestDTO(
        @Schema(description = "Nota de 1 a 5", example = "5")
        @NotNull @Min(1) @Max(5) Integer nota,

        @Schema(description = "Comentário opcional", example = "Obra-prima, trilha sonora incrível!")
        @Size(max = 500) String comentario,

        @Schema(description = "Id do filme avaliado (deve existir)", example = "1")
        @NotNull Long filmeId,

        @Schema(description = "Id do usuário avaliador (deve existir)", example = "1")
        @NotNull Long usuarioId) {
}
