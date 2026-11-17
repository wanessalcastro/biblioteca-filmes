package com.exemplo.bibliotecafilmes.dto;

import com.exemplo.bibliotecafilmes.entity.NivelAcesso;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Resposta da geração de uma chave: é a ÚNICA vez em que o valor secreto é exibido")
public record ApiKeyCriadaResponseDTO(
        @Schema(description = "Identificador", example = "3") Long id,
        @Schema(description = "Valor secreto da chave — envie no cabeçalho X-API-Key. Guarde-o: não poderá ser consultado depois",
                example = "bf_Zk3n9Qx1v0Yb7Tq2LmA8sWc4RdE6HuJp5NfGt1VoXyI") String chave,
        @Schema(description = "Primeiros caracteres da chave", example = "bf_Zk3n9Qx") String prefixo,
        @Schema(description = "Descrição", example = "Integração do site") String descricao,
        @Schema(description = "Nível de acesso", example = "USUARIO") NivelAcesso nivel,
        @Schema(description = "Se a chave está ativa", example = "true") Boolean ativa,
        @Schema(description = "Data de criação", example = "2026-11-17T10:30:00") LocalDateTime criadaEm,
        @Schema(description = "Data de expiração (nulo = não expira)", example = "2027-12-31T23:59:59") LocalDateTime expiraEm,
        @Schema(description = "Id do usuário dono da chave", example = "1") Long usuarioId) {
}
