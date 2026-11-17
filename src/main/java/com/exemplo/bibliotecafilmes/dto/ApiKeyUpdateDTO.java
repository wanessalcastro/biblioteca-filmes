package com.exemplo.bibliotecafilmes.dto;

import com.exemplo.bibliotecafilmes.entity.NivelAcesso;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Schema(description = "Dados para atualizar uma chave de API existente (a chave em si não muda)")
public record ApiKeyUpdateDTO(
        @Schema(description = "Descrição da chave", example = "Integração do site (produção)")
        @NotBlank @Size(min = 2, max = 100) String descricao,

        @Schema(description = "Nível de acesso", example = "USUARIO")
        @NotNull NivelAcesso nivel,

        @Schema(description = "Se a chave está ativa (false = revogada)", example = "true")
        @NotNull Boolean ativa,

        @Schema(description = "Validade da chave (opcional)", example = "2027-12-31T23:59:59")
        @Future LocalDateTime expiraEm) {
}
