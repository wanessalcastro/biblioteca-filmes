package com.exemplo.bibliotecafilmes.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Corpo padrão das respostas de erro")
public record ErroResponseDTO(
        @Schema(description = "Momento do erro", example = "2026-10-13T20:15:30") LocalDateTime timestamp,
        @Schema(description = "Código HTTP", example = "400") int status,
        @Schema(description = "Descrição do status HTTP", example = "Bad Request") String erro,
        @Schema(description = "Mensagem explicativa", example = "Dados de entrada inválidos") String mensagem,
        @Schema(description = "Caminho da requisição", example = "/usuarios") String caminho,
        @Schema(description = "Lista de campos inválidos (apenas em erros de validação)") List<CampoInvalidoDTO> campos) {
}
