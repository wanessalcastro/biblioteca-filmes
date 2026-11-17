package com.exemplo.bibliotecafilmes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Dados para criar ou atualizar um usuário")
public record UsuarioRequestDTO(
        @Schema(description = "Nome do usuário", example = "Maria Silva")
        @NotBlank @Size(min = 2, max = 120) String nome,

        @Schema(description = "E-mail (único)", example = "maria.silva@email.com")
        @NotBlank @Email @Size(max = 150) String email,

        @Schema(description = "Data de nascimento (yyyy-MM-dd)", example = "1995-04-12")
        @NotNull @Past LocalDate dataNascimento) {
}
