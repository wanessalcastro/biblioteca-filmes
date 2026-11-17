package com.exemplo.bibliotecafilmes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Dados para criar ou atualizar um diretor")
public record DiretorRequestDTO(
        @Schema(description = "Nome completo do diretor", example = "Christopher Nolan")
        @NotBlank @Size(min = 2, max = 120) String nome,

        @Schema(description = "Nacionalidade", example = "Britânico")
        @NotBlank @Size(max = 60) String nacionalidade,

        @Schema(description = "Data de nascimento (yyyy-MM-dd), deve estar no passado", example = "1970-07-30")
        @NotNull @Past LocalDate dataNascimento) {
}
