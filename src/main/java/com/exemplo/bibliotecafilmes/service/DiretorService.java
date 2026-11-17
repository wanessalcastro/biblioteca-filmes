package com.exemplo.bibliotecafilmes.service;

import com.exemplo.bibliotecafilmes.dto.DiretorRequestDTO;
import com.exemplo.bibliotecafilmes.dto.DiretorResponseDTO;
import com.exemplo.bibliotecafilmes.entity.Diretor;
import com.exemplo.bibliotecafilmes.exception.ConflictException;
import com.exemplo.bibliotecafilmes.exception.ResourceNotFoundException;
import com.exemplo.bibliotecafilmes.repository.DiretorRepository;
import com.exemplo.bibliotecafilmes.repository.FilmeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DiretorService {

    private final DiretorRepository repository;
    private final FilmeRepository filmeRepository;

    public DiretorService(DiretorRepository repository, FilmeRepository filmeRepository) {
        this.repository = repository;
        this.filmeRepository = filmeRepository;
    }

    @Transactional
    public DiretorResponseDTO criar(DiretorRequestDTO dto) {
        Diretor diretor = new Diretor();
        copiar(dto, diretor);
        return toResponse(repository.save(diretor));
    }

    @Transactional(readOnly = true)
    public Page<DiretorResponseDTO> listar(Pageable pageable) {
        return repository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public DiretorResponseDTO buscarPorId(Long id) {
        return toResponse(obter(id));
    }

    @Transactional
    public DiretorResponseDTO atualizar(Long id, DiretorRequestDTO dto) {
        Diretor diretor = obter(id);
        copiar(dto, diretor);
        return toResponse(repository.save(diretor));
    }

    @Transactional
    public void remover(Long id) {
        Diretor diretor = obter(id);
        if (filmeRepository.existsByDiretorId(id)) {
            throw new ConflictException("Não é possível remover o diretor: existem filmes associados a ele");
        }
        repository.delete(diretor);
    }

    @Transactional(readOnly = true)
    public Page<DiretorResponseDTO> listarPorNacionalidade(String nacionalidade, Pageable pageable) {
        return repository.findByNacionalidadeIgnoreCase(nacionalidade, pageable).map(this::toResponse);
    }

    
    private Diretor obter(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Diretor", id));
    }

    private void copiar(DiretorRequestDTO dto, Diretor diretor) {
        diretor.setNome(dto.nome());
        diretor.setNacionalidade(dto.nacionalidade());
        diretor.setDataNascimento(dto.dataNascimento());
    }

    private DiretorResponseDTO toResponse(Diretor d) {
        return new DiretorResponseDTO(d.getId(), d.getNome(), d.getNacionalidade(), d.getDataNascimento());
    }
}
