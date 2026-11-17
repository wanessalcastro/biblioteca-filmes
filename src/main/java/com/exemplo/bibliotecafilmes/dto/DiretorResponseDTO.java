package com.exemplo.bibliotecafilmes.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Diretor retornado pela API")
public record DiretorResponseDTO(
        @Schema(description = "Identificador", example = "1") Long id,
        @Schema(description = "Nome completo", example = "Christopher Nolan") String nome,
        @Schema(description = "Nacionalidade", example = "Britânico") String nacionalidade,
        @Schema(description = "Data de nascimento", example = "1970-07-30") LocalDate dataNascimento) {
}
