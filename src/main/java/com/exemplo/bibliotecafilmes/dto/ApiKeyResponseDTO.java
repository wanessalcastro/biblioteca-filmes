package com.exemplo.bibliotecafilmes.dto;

import com.exemplo.bibliotecafilmes.entity.NivelAcesso;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Chave de API retornada pela API (sem o valor secreto)")
public record ApiKeyResponseDTO(
        @Schema(description = "Identificador", example = "3") Long id,
        @Schema(description = "Primeiros caracteres da chave, para identificação", example = "bf_aB3dE9") String prefixo,
        @Schema(description = "Descrição", example = "Integração do site") String descricao,
        @Schema(description = "Nível de acesso", example = "USUARIO") NivelAcesso nivel,
        @Schema(description = "Se a chave está ativa", example = "true") Boolean ativa,
        @Schema(description = "Data de criação", example = "2026-11-17T10:30:00") LocalDateTime criadaEm,
        @Schema(description = "Data de expiração (nulo = não expira)", example = "2027-12-31T23:59:59") LocalDateTime expiraEm,
        @Schema(description = "Id do usuário dono da chave", example = "1") Long usuarioId) {
}
