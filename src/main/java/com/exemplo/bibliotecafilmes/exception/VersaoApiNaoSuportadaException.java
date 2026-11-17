package com.exemplo.bibliotecafilmes.exception;

import java.util.Collection;

/** Valor inválido em X-API-Version — mapeada para HTTP 400. */
public class VersaoApiNaoSuportadaException extends RuntimeException {

    public VersaoApiNaoSuportadaException(String versao, Collection<String> suportadas) {
        super("Versão de API não suportada: '" + versao + "'. Versões suportadas: "
                + String.join(", ", suportadas));
    }
}
