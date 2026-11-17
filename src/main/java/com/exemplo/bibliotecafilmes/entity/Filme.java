package com.exemplo.bibliotecafilmes.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "filmes")
public class Filme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 1, max = 150)
    @Column(nullable = false, length = 150)
    private String titulo;

    @NotNull
    @Min(1888)
    @Max(2100)
    @Column(name = "ano_lancamento", nullable = false)
    private Integer anoLancamento;

    @NotNull
    @Min(1)
    @Max(600)
    @Column(name = "duracao_minutos", nullable = false)
    private Integer duracaoMinutos;

    @Size(max = 1000)
    @Column(length = 1000)
    private String sinopse;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClassificacaoIndicativa classificacao;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "diretor_id", nullable = false)
    private Diretor diretor;

    @NotEmpty
    @ManyToMany
    @JoinTable(name = "filme_genero",
            joinColumns = @JoinColumn(name = "filme_id"),
            inverseJoinColumns = @JoinColumn(name = "genero_id"))
    private Set<Genero> generos = new HashSet<>();

    /** Lado inverso: ao remover o filme, suas avaliações são removidas junto. */
    @OneToMany(mappedBy = "filme", cascade = CascadeType.REMOVE)
    private Set<Avaliacao> avaliacoes = new HashSet<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public Integer getAnoLancamento() { return anoLancamento; }
    public void setAnoLancamento(Integer anoLancamento) { this.anoLancamento = anoLancamento; }
    public Integer getDuracaoMinutos() { return duracaoMinutos; }
    public void setDuracaoMinutos(Integer duracaoMinutos) { this.duracaoMinutos = duracaoMinutos; }
    public String getSinopse() { return sinopse; }
    public void setSinopse(String sinopse) { this.sinopse = sinopse; }
    public ClassificacaoIndicativa getClassificacao() { return classificacao; }
    public void setClassificacao(ClassificacaoIndicativa classificacao) { this.classificacao = classificacao; }
    public Diretor getDiretor() { return diretor; }
    public void setDiretor(Diretor diretor) { this.diretor = diretor; }
    public Set<Genero> getGeneros() { return generos; }
    public void setGeneros(Set<Genero> generos) { this.generos = generos; }
    public Set<Avaliacao> getAvaliacoes() { return avaliacoes; }
    public void setAvaliacoes(Set<Avaliacao> avaliacoes) { this.avaliacoes = avaliacoes; }
}
