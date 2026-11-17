package com.exemplo.bibliotecafilmes.dto;

import com.exemplo.bibliotecafilmes.entity.ClassificacaoIndicativa;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Filme retornado pela API na versão 2 (X-API-Version: 2): formato enriquecido")
public record FilmeResponseV2DTO(
        @Schema(description = "Identificador", example = "1") Long id,
        @Schema(description = "Título", example = "Interestelar") String titulo,
        @Schema(description = "Ano de lançamento", example = "2014") Integer anoLancamento,
        @Schema(description = "Duração em minutos", example = "169") Integer duracaoMinutos,
        @Schema(description = "Duração legível", example = "2h49min") String duracaoFormatada,
        @Schema(description = "Sinopse") String sinopse,
        @Schema(description = "Classificação indicativa", example = "DEZ") ClassificacaoIndicativa classificacao,
        @Schema(description = "Diretor (resumo)") DiretorResumo diretor,
        @Schema(description = "Nomes dos gêneros", example = "[\"Ficção Científica\", \"Drama\"]") List<String> generos,
        @Schema(description = "Média das notas (nulo se não houver avaliações)", example = "4.0") Double mediaAvaliacoes,
        @Schema(description = "Quantidade de avaliações", example = "2") long totalAvaliacoes) {

    @Schema(description = "Resumo do diretor")
    public record DiretorResumo(
            @Schema(description = "Id do diretor", example = "1") Long id,
            @Schema(description = "Nome do diretor", example = "Christopher Nolan") String nome) {
    }
}
