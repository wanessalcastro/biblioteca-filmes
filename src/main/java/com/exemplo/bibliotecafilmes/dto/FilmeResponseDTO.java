package com.exemplo.bibliotecafilmes.dto;

import com.exemplo.bibliotecafilmes.entity.ClassificacaoIndicativa;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Filme retornado pela API")
public record FilmeResponseDTO(
        @Schema(description = "Identificador", example = "1") Long id,
        @Schema(description = "Título", example = "Interestelar") String titulo,
        @Schema(description = "Ano de lançamento", example = "2014") Integer anoLancamento,
        @Schema(description = "Duração em minutos", example = "169") Integer duracaoMinutos,
        @Schema(description = "Sinopse", example = "Exploradores viajam através de um buraco de minhoca para salvar a humanidade.") String sinopse,
        @Schema(description = "Classificação indicativa", example = "DEZ") ClassificacaoIndicativa classificacao,
        @Schema(description = "Id do diretor", example = "1") Long diretorId,
        @Schema(description = "Nome do diretor", example = "Christopher Nolan") String diretorNome,
        @Schema(description = "Gêneros do filme") List<GeneroResponseDTO> generos) {
}
