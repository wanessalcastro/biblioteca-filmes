package com.exemplo.bibliotecafilmes.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/** Chave de API de um usuário. A chave em texto puro nunca é guardada: só o hash SHA-256. */
@Entity
@Table(name = "api_keys", uniqueConstraints = @UniqueConstraint(columnNames = "chave_hash"))
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Primeiros caracteres da chave, só para o dono identificá-la numa listagem. */
    @NotBlank
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String prefixo;

    @NotBlank
    @Size(min = 64, max = 64)
    @Column(name = "chave_hash", nullable = false, length = 64)
    private String chaveHash;

    @NotBlank
    @Size(min = 2, max = 100)
    @Column(nullable = false, length = 100)
    private String descricao;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NivelAcesso nivel;

    @NotNull
    @Column(nullable = false)
    private Boolean ativa = true;

    @NotNull
    @Column(name = "criada_em", nullable = false)
    private LocalDateTime criadaEm;

    @Column(name = "expira_em")
    private LocalDateTime expiraEm;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    public boolean estaValida() {
        return Boolean.TRUE.equals(ativa) && (expiraEm == null || expiraEm.isAfter(LocalDateTime.now()));
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPrefixo() { return prefixo; }
    public void setPrefixo(String prefixo) { this.prefixo = prefixo; }
    public String getChaveHash() { return chaveHash; }
    public void setChaveHash(String chaveHash) { this.chaveHash = chaveHash; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public NivelAcesso getNivel() { return nivel; }
    public void setNivel(NivelAcesso nivel) { this.nivel = nivel; }
    public Boolean getAtiva() { return ativa; }
    public void setAtiva(Boolean ativa) { this.ativa = ativa; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
    public void setCriadaEm(LocalDateTime criadaEm) { this.criadaEm = criadaEm; }
    public LocalDateTime getExpiraEm() { return expiraEm; }
    public void setExpiraEm(LocalDateTime expiraEm) { this.expiraEm = expiraEm; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}
