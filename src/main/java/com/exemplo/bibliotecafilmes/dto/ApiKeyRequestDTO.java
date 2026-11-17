package com.exemplo.bibliotecafilmes.dto;

import com.exemplo.bibliotecafilmes.entity.NivelAcesso;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Schema(description = "Dados para gerar uma nova chave de API")
public record ApiKeyRequestDTO(
        @Schema(description = "Id do usuário dono da chave", example = "1")
        @NotNull Long usuarioId,

        @Schema(description = "Descrição para identificar o uso da chave", example = "Integração do site")
        @NotBlank @Size(min = 2, max = 100) String descricao,

        @Schema(description = "Nível de acesso: USUARIO (POST/PUT) ou ADMIN (também DELETE e /api-keys)", example = "USUARIO")
        @NotNull NivelAcesso nivel,

        @Schema(description = "Validade da chave (opcional; sem valor, não expira)", example = "2027-12-31T23:59:59")
        @Future LocalDateTime expiraEm) {
}
