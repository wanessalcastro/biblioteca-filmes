package com.exemplo.bibliotecafilmes.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Usuário retornado pela API")
public record UsuarioResponseDTO(
        @Schema(description = "Identificador", example = "1") Long id,
        @Schema(description = "Nome", example = "Maria Silva") String nome,
        @Schema(description = "E-mail", example = "maria.silva@email.com") String email,
        @Schema(description = "Data de nascimento", example = "1995-04-12") LocalDate dataNascimento) {
}
