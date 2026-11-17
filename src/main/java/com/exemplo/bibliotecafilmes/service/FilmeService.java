package com.exemplo.bibliotecafilmes.service;

import com.exemplo.bibliotecafilmes.dto.FilmeRequestDTO;
import com.exemplo.bibliotecafilmes.dto.FilmeResponseDTO;
import com.exemplo.bibliotecafilmes.dto.FilmeResponseV2DTO;
import com.exemplo.bibliotecafilmes.entity.Diretor;
import com.exemplo.bibliotecafilmes.entity.Filme;
import com.exemplo.bibliotecafilmes.entity.Genero;
import com.exemplo.bibliotecafilmes.exception.ResourceNotFoundException;
import com.exemplo.bibliotecafilmes.repository.AvaliacaoRepository;
import com.exemplo.bibliotecafilmes.repository.DiretorRepository;
import com.exemplo.bibliotecafilmes.repository.FilmeRepository;
import com.exemplo.bibliotecafilmes.repository.GeneroRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class FilmeService {

    private final FilmeRepository repository;
    private final DiretorRepository diretorRepository;
    private final GeneroRepository generoRepository;
    private final AvaliacaoRepository avaliacaoRepository;

    public FilmeService(FilmeRepository repository, DiretorRepository diretorRepository,
                        GeneroRepository generoRepository, AvaliacaoRepository avaliacaoRepository) {
        this.repository = repository;
        this.diretorRepository = diretorRepository;
        this.generoRepository = generoRepository;
        this.avaliacaoRepository = avaliacaoRepository;
    }

    @Transactional
    public FilmeResponseDTO criar(FilmeRequestDTO dto) {
        Filme filme = new Filme();
        copiar(dto, filme);
        return toResponse(repository.save(filme));
    }

    @Transactional(readOnly = true)
    public Page<FilmeResponseDTO> listar(Pageable pageable) {
        return repository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public FilmeResponseDTO buscarPorId(Long id) {
        return toResponse(obter(id));
    }

    /** Versão 2 do detalhe do filme: inclui duração formatada, média e total de avaliações. */
    @Transactional(readOnly = true)
    public FilmeResponseV2DTO buscarPorIdV2(Long id) {
        Filme f = obter(id);
        Double media = avaliacaoRepository.mediaNotaPorFilme(id);
        Double mediaArredondada = media == null ? null : Math.round(media * 10) / 10.0;
        return new FilmeResponseV2DTO(f.getId(), f.getTitulo(), f.getAnoLancamento(), f.getDuracaoMinutos(),
                formatarDuracao(f.getDuracaoMinutos()), f.getSinopse(), f.getClassificacao(),
                new FilmeResponseV2DTO.DiretorResumo(f.getDiretor().getId(), f.getDiretor().getNome()),
                f.getGeneros().stream()
                        .sorted(Comparator.comparing(Genero::getId))
                        .map(Genero::getNome)
                        .toList(),
                mediaArredondada, avaliacaoRepository.countByFilmeId(id));
    }

    private static String formatarDuracao(int minutos) {
        if (minutos < 60) {
            return minutos + "min";
        }
        int resto = minutos % 60;
        return (minutos / 60) + "h" + (resto == 0 ? "" : String.format("%02dmin", resto));
    }

    @Transactional
    public FilmeResponseDTO atualizar(Long id, FilmeRequestDTO dto) {
        Filme filme = obter(id);
        copiar(dto, filme);
        return toResponse(repository.save(filme));
    }

    @Transactional
    public void remover(Long id) {
        repository.delete(obter(id));
    }

    @Transactional(readOnly = true)
    public Page<FilmeResponseDTO> buscarPorTitulo(String titulo, Pageable pageable) {
        return repository.findByTituloContainingIgnoreCase(titulo, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<FilmeResponseDTO> listarPorAno(Integer ano, Pageable pageable) {
        return repository.findByAnoLancamento(ano, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<FilmeResponseDTO> listarPorGenero(Long generoId, Pageable pageable) {
        if (!generoRepository.existsById(generoId)) {
            throw new ResourceNotFoundException("Gênero", generoId);
        }
        return repository.findByGenerosId(generoId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<FilmeResponseDTO> listarPorDiretor(Long diretorId, Pageable pageable) {
        if (!diretorRepository.existsById(diretorId)) {
            throw new ResourceNotFoundException("Diretor", diretorId);
        }
        return repository.findByDiretorId(diretorId, pageable).map(this::toResponse);
    }

    private Filme obter(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Filme", id));
    }

    private void copiar(FilmeRequestDTO dto, Filme filme) {
        Diretor diretor = diretorRepository.findById(dto.diretorId())
                .orElseThrow(() -> new ResourceNotFoundException("Diretor", dto.diretorId()));

        Set<Long> idsGeneros = new HashSet<>(dto.generoIds());
        List<Genero> generos = generoRepository.findAllById(idsGeneros);
        if (generos.size() != idsGeneros.size()) {
            Set<Long> encontrados = new HashSet<>();
            generos.forEach(g -> encontrados.add(g.getId()));
            Long ausente = idsGeneros.stream().filter(i -> !encontrados.contains(i)).findFirst().orElse(null);
            throw new ResourceNotFoundException("Gênero", ausente);
        }

        filme.setTitulo(dto.titulo());
        filme.setAnoLancamento(dto.anoLancamento());
        filme.setDuracaoMinutos(dto.duracaoMinutos());
        filme.setSinopse(dto.sinopse());
        filme.setClassificacao(dto.classificacao());
        filme.setDiretor(diretor);
        filme.getGeneros().clear();
        filme.getGeneros().addAll(generos);
    }

    private FilmeResponseDTO toResponse(Filme f) {
        return new FilmeResponseDTO(f.getId(), f.getTitulo(), f.getAnoLancamento(), f.getDuracaoMinutos(),
                f.getSinopse(), f.getClassificacao(), f.getDiretor().getId(), f.getDiretor().getNome(),
                f.getGeneros().stream()
                        .sorted(Comparator.comparing(Genero::getId))
                        .map(GeneroService::toResponse)
                        .toList());
    }
}
