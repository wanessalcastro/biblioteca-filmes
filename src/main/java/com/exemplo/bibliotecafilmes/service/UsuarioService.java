package com.exemplo.bibliotecafilmes.service;

import com.exemplo.bibliotecafilmes.dto.UsuarioRequestDTO;
import com.exemplo.bibliotecafilmes.dto.UsuarioResponseDTO;
import com.exemplo.bibliotecafilmes.entity.Usuario;
import com.exemplo.bibliotecafilmes.exception.ConflictException;
import com.exemplo.bibliotecafilmes.exception.ResourceNotFoundException;
import com.exemplo.bibliotecafilmes.repository.ApiKeyRepository;
import com.exemplo.bibliotecafilmes.repository.AvaliacaoRepository;
import com.exemplo.bibliotecafilmes.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final ApiKeyRepository apiKeyRepository;

    public UsuarioService(UsuarioRepository repository, AvaliacaoRepository avaliacaoRepository,
                          ApiKeyRepository apiKeyRepository) {
        this.repository = repository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.apiKeyRepository = apiKeyRepository;
    }

    @Transactional
    public UsuarioResponseDTO criar(UsuarioRequestDTO dto) {
        if (repository.existsByEmailIgnoreCase(dto.email())) {
            throw new ConflictException("Já existe um usuário com o e-mail '" + dto.email() + "'");
        }
        Usuario usuario = new Usuario();
        copiar(dto, usuario);
        return toResponse(repository.save(usuario));
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponseDTO> listar(Pageable pageable) {
        return repository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(Long id) {
        return toResponse(obter(id));
    }

    @Transactional
    public UsuarioResponseDTO atualizar(Long id, UsuarioRequestDTO dto) {
        Usuario usuario = obter(id);
        if (repository.existsByEmailIgnoreCaseAndIdNot(dto.email(), id)) {
            throw new ConflictException("Já existe um usuário com o e-mail '" + dto.email() + "'");
        }
        copiar(dto, usuario);
        return toResponse(repository.save(usuario));
    }

    @Transactional
    public void remover(Long id) {
        Usuario usuario = obter(id);
        if (avaliacaoRepository.existsByUsuarioId(id)) {
            throw new ConflictException("Não é possível remover o usuário: existem avaliações feitas por ele");
        }
        if (apiKeyRepository.existsByUsuarioId(id)) {
            throw new ConflictException("Não é possível remover o usuário: existem chaves de API associadas a ele");
        }
        repository.delete(usuario);
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponseDTO> buscarPorEmail(String email, Pageable pageable) {
        return repository.findByEmailContainingIgnoreCase(email, pageable).map(this::toResponse);
    }

    private Usuario obter(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", id));
    }

    private void copiar(UsuarioRequestDTO dto, Usuario usuario) {
        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());
        usuario.setDataNascimento(dto.dataNascimento());
    }

    private UsuarioResponseDTO toResponse(Usuario u) {
        return new UsuarioResponseDTO(u.getId(), u.getNome(), u.getEmail(), u.getDataNascimento());
    }
}
