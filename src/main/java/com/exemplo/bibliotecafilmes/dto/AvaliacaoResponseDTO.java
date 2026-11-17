package com.exemplo.bibliotecafilmes.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Avaliação retornada pela API")
public record AvaliacaoResponseDTO(
        @Schema(description = "Identificador", example = "1") Long id,
        @Schema(description = "Nota de 1 a 5", example = "5") Integer nota,
        @Schema(description = "Comentário", example = "Obra-prima, trilha sonora incrível!") String comentario,
        @Schema(description = "Data da avaliação", example = "2026-10-13") LocalDate dataAvaliacao,
        @Schema(description = "Id do filme", example = "1") Long filmeId,
        @Schema(description = "Título do filme", example = "Interestelar") String filmeTitulo,
        @Schema(description = "Id do usuário", example = "1") Long usuarioId,
        @Schema(description = "Nome do usuário", example = "Maria Silva") String usuarioNome) {
}
