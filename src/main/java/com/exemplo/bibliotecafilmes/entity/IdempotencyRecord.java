package com.exemplo.bibliotecafilmes.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Resposta guardada de uma requisição POST feita com {@code X-Idempotency-Key}.
 * A {@code chave} combina o id da chave de API e o valor do cabeçalho, de modo que clientes
 * diferentes nunca colidem entre si.
 */
@Entity
@Table(name = "idempotency_records", uniqueConstraints = @UniqueConstraint(columnNames = "chave"))
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String chave;

    /** SHA-256 de método + URI + corpo, para detectar reutilização da chave com outro conteúdo. */
    @Column(name = "impressao_digital", nullable = false, length = 64)
    private String impressaoDigital;

    @Column(name = "status_http", nullable = false)
    private int statusHttp;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(length = 500)
    private String location;

    @Column(length = 65536)
    private String corpo;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    protected IdempotencyRecord() {
    }

    public IdempotencyRecord(String chave, String impressaoDigital, int statusHttp,
                             String contentType, String location, String corpo) {
        this.chave = chave;
        this.impressaoDigital = impressaoDigital;
        this.statusHttp = statusHttp;
        this.contentType = contentType;
        this.location = location;
        this.corpo = corpo;
    }

    public Long getId() { return id; }
    public String getChave() { return chave; }
    public String getImpressaoDigital() { return impressaoDigital; }
    public int getStatusHttp() { return statusHttp; }
    public String getContentType() { return contentType; }
    public String getLocation() { return location; }
    public String getCorpo() { return corpo; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}
