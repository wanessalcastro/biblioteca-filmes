package com.exemplo.bibliotecafilmes.service;

import com.exemplo.bibliotecafilmes.dto.GeneroRequestDTO;
import com.exemplo.bibliotecafilmes.dto.GeneroResponseDTO;
import com.exemplo.bibliotecafilmes.entity.Genero;
import com.exemplo.bibliotecafilmes.exception.ConflictException;
import com.exemplo.bibliotecafilmes.exception.ResourceNotFoundException;
import com.exemplo.bibliotecafilmes.repository.FilmeRepository;
import com.exemplo.bibliotecafilmes.repository.GeneroRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GeneroService {

    private final GeneroRepository repository;
    private final FilmeRepository filmeRepository;

    public GeneroService(GeneroRepository repository, FilmeRepository filmeRepository) {
        this.repository = repository;
        this.filmeRepository = filmeRepository;
    }

    @Transactional
    public GeneroResponseDTO criar(GeneroRequestDTO dto) {
        if (repository.existsByNomeIgnoreCase(dto.nome())) {
            throw new ConflictException("Já existe um gênero com o nome '" + dto.nome() + "'");
        }
        Genero genero = new Genero();
        copiar(dto, genero);
        return toResponse(repository.save(genero));
    }

    @Transactional(readOnly = true)
    public Page<GeneroResponseDTO> listar(Pageable pageable) {
        return repository.findAll(pageable).map(GeneroService::toResponse);
    }

    @Transactional(readOnly = true)
    public GeneroResponseDTO buscarPorId(Long id) {
        return toResponse(obter(id));
    }

    @Transactional
    public GeneroResponseDTO atualizar(Long id, GeneroRequestDTO dto) {
        Genero genero = obter(id);
        if (repository.existsByNomeIgnoreCaseAndIdNot(dto.nome(), id)) {
            throw new ConflictException("Já existe um gênero com o nome '" + dto.nome() + "'");
        }
        copiar(dto, genero);
        return toResponse(repository.save(genero));
    }

    @Transactional
    public void remover(Long id) {
        Genero genero = obter(id);
        if (filmeRepository.existsByGenerosId(id)) {
            throw new ConflictException("Não é possível remover o gênero: existem filmes associados a ele");
        }
        repository.delete(genero);
    }

    @Transactional(readOnly = true)
    public Page<GeneroResponseDTO> listarPorFilme(Long filmeId, Pageable pageable) {
        if (!filmeRepository.existsById(filmeId)) {
            throw new ResourceNotFoundException("Filme", filmeId);
        }
        return repository.findByFilmeId(filmeId, pageable).map(GeneroService::toResponse);
    }

    private Genero obter(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gênero", id));
    }

    private void copiar(GeneroRequestDTO dto, Genero genero) {
        genero.setNome(dto.nome());
        genero.setDescricao(dto.descricao());
    }

    static GeneroResponseDTO toResponse(Genero g) {
        return new GeneroResponseDTO(g.getId(), g.getNome(), g.getDescricao());
    }
}
