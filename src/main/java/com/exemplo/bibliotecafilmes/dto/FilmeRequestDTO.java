package com.exemplo.bibliotecafilmes.dto;

import com.exemplo.bibliotecafilmes.entity.ClassificacaoIndicativa;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

@Schema(description = "Dados para criar ou atualizar um filme")
public record FilmeRequestDTO(
        @Schema(description = "Título do filme", example = "Interestelar")
        @NotBlank @Size(max = 150) String titulo,

        @Schema(description = "Ano de lançamento", example = "2014")
        @NotNull @Min(1888) @Max(2100) Integer anoLancamento,

        @Schema(description = "Duração em minutos", example = "169")
        @NotNull @Min(1) @Max(600) Integer duracaoMinutos,

        @Schema(description = "Sinopse (opcional)", example = "Exploradores viajam através de um buraco de minhoca para salvar a humanidade.")
        @Size(max = 1000) String sinopse,

        @Schema(description = "Classificação indicativa", example = "DEZ")
        @NotNull ClassificacaoIndicativa classificacao,

        @Schema(description = "Id do diretor (deve existir)", example = "1")
        @NotNull Long diretorId,

        @Schema(description = "Ids dos gêneros (ao menos um; todos devem existir)", example = "[1, 2]")
        @NotEmpty Set<Long> generoIds) {
}
