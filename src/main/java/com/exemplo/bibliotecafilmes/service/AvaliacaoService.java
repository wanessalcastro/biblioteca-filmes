package com.exemplo.bibliotecafilmes.service;

import com.exemplo.bibliotecafilmes.dto.AvaliacaoRequestDTO;
import com.exemplo.bibliotecafilmes.dto.AvaliacaoResponseDTO;
import com.exemplo.bibliotecafilmes.entity.Avaliacao;
import com.exemplo.bibliotecafilmes.entity.Filme;
import com.exemplo.bibliotecafilmes.entity.Usuario;
import com.exemplo.bibliotecafilmes.exception.ConflictException;
import com.exemplo.bibliotecafilmes.exception.ResourceNotFoundException;
import com.exemplo.bibliotecafilmes.repository.AvaliacaoRepository;
import com.exemplo.bibliotecafilmes.repository.FilmeRepository;
import com.exemplo.bibliotecafilmes.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class AvaliacaoService {

    private final AvaliacaoRepository repository;
    private final FilmeRepository filmeRepository;
    private final UsuarioRepository usuarioRepository;

    public AvaliacaoService(AvaliacaoRepository repository, FilmeRepository filmeRepository,
                            UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.filmeRepository = filmeRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public AvaliacaoResponseDTO criar(AvaliacaoRequestDTO dto) {
        if (repository.existsByFilmeIdAndUsuarioId(dto.filmeId(), dto.usuarioId())) {
            throw new ConflictException("Este usuário já avaliou este filme");
        }
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setDataAvaliacao(LocalDate.now());
        copiar(dto, avaliacao);
        return toResponse(repository.save(avaliacao));
    }

    @Transactional(readOnly = true)
    public Page<AvaliacaoResponseDTO> listar(Pageable pageable) {
        return repository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public AvaliacaoResponseDTO buscarPorId(Long id) {
        return toResponse(obter(id));
    }

    @Transactional
    public AvaliacaoResponseDTO atualizar(Long id, AvaliacaoRequestDTO dto) {
        Avaliacao avaliacao = obter(id);
        if (repository.existsByFilmeIdAndUsuarioIdAndIdNot(dto.filmeId(), dto.usuarioId(), id)) {
            throw new ConflictException("Este usuário já avaliou este filme");
        }
        copiar(dto, avaliacao);
        return toResponse(repository.save(avaliacao));
    }

    @Transactional
    public void remover(Long id) {
        repository.delete(obter(id));
    }

    @Transactional(readOnly = true)
    public Page<AvaliacaoResponseDTO> listarPorFilme(Long filmeId, Pageable pageable) {
        if (!filmeRepository.existsById(filmeId)) {
            throw new ResourceNotFoundException("Filme", filmeId);
        }
        return repository.findByFilmeId(filmeId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AvaliacaoResponseDTO> listarPorUsuario(Long usuarioId, Pageable pageable) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException("Usuário", usuarioId);
        }
        return repository.findByUsuarioId(usuarioId, pageable).map(this::toResponse);
    }

    private Avaliacao obter(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Avaliação", id));
    }

    private void copiar(AvaliacaoRequestDTO dto, Avaliacao avaliacao) {
        Filme filme = filmeRepository.findById(dto.filmeId())
                .orElseThrow(() -> new ResourceNotFoundException("Filme", dto.filmeId()));
        Usuario usuario = usuarioRepository.findById(dto.usuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", dto.usuarioId()));
        avaliacao.setNota(dto.nota());
        avaliacao.setComentario(dto.comentario());
        avaliacao.setFilme(filme);
        avaliacao.setUsuario(usuario);
    }

    private AvaliacaoResponseDTO toResponse(Avaliacao a) {
        return new AvaliacaoResponseDTO(a.getId(), a.getNota(), a.getComentario(), a.getDataAvaliacao(),
                a.getFilme().getId(), a.getFilme().getTitulo(),
                a.getUsuario().getId(), a.getUsuario().getNome());
    }
}
