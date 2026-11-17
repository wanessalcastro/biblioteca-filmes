package com.exemplo.bibliotecafilmes.filter;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/** Requisição cujo corpo já foi lido para memória, permitindo calcular o hash e ainda assim repassá-lo adiante. */
class CachedBodyRequest extends HttpServletRequestWrapper {

    private final byte[] corpo;

    CachedBodyRequest(HttpServletRequest request, byte[] corpo) {
        super(request);
        this.corpo = corpo;
    }

    @Override
    public ServletInputStream getInputStream() {
        ByteArrayInputStream origem = new ByteArrayInputStream(corpo);
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return origem.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener listener) {
                throw new UnsupportedOperationException("Leitura assíncrona não suportada");
            }

            @Override
            public int read() {
                return origem.read();
            }

            @Override
            public int read(byte[] b, int off, int len) {
                return origem.read(b, off, len);
            }
        };
    }

    @Override
    public BufferedReader getReader() {
        String codificacao = getCharacterEncoding();
        Charset charset = codificacao == null ? StandardCharsets.UTF_8 : Charset.forName(codificacao);
        return new BufferedReader(new InputStreamReader(getInputStream(), charset));
    }
}
