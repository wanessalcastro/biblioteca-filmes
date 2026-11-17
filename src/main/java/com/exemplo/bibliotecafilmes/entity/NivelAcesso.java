package com.exemplo.bibliotecafilmes.entity;

/**
 * Nível de acesso de uma chave de API. A ordem importa: um nível maior inclui os menores.
 * <ul>
 *   <li>{@link #USUARIO}: pode criar e atualizar recursos (POST/PUT);</li>
 *   <li>{@link #ADMIN}: tudo do USUARIO + remoções (DELETE) e gerenciamento de chaves ({@code /api-keys}).</li>
 * </ul>
 * Consultas (GET) dos recursos são públicas.
 */
public enum NivelAcesso {
    USUARIO,
    ADMIN;

    public boolean permite(NivelAcesso exigido) {
        return this.ordinal() >= exigido.ordinal();
    }
}
