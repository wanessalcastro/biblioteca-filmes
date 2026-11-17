package com.exemplo.bibliotecafilmes.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Campo que falhou na validação")
public record CampoInvalidoDTO(
        @Schema(description = "Nome do campo", example = "email") String campo,
        @Schema(description = "Motivo da rejeição", example = "deve ser um endereço de e-mail bem formado") String mensagem) {
}
