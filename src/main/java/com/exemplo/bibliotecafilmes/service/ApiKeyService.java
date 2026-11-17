package com.exemplo.bibliotecafilmes.service;

import com.exemplo.bibliotecafilmes.dto.ApiKeyCriadaResponseDTO;
import com.exemplo.bibliotecafilmes.dto.ApiKeyRequestDTO;
import com.exemplo.bibliotecafilmes.dto.ApiKeyResponseDTO;
import com.exemplo.bibliotecafilmes.dto.ApiKeyUpdateDTO;
import com.exemplo.bibliotecafilmes.entity.ApiKey;
import com.exemplo.bibliotecafilmes.entity.NivelAcesso;
import com.exemplo.bibliotecafilmes.entity.Usuario;
import com.exemplo.bibliotecafilmes.exception.ApiKeyInvalidaException;
import com.exemplo.bibliotecafilmes.exception.ResourceNotFoundException;
import com.exemplo.bibliotecafilmes.repository.ApiKeyRepository;
import com.exemplo.bibliotecafilmes.repository.UsuarioRepository;
import com.exemplo.bibliotecafilmes.util.HashUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class ApiKeyService {

    private static final String PREFIXO_CHAVE = "bf_";
    private static final int TAMANHO_PREFIXO_EXIBIDO = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    /** Identidade resolvida a partir de uma chave válida (usada pelo filtro de autenticação). */
    public record Credencial(Long id, NivelAcesso nivel) {
    }

    private final ApiKeyRepository repository;
    private final UsuarioRepository usuarioRepository;

    public ApiKeyService(ApiKeyRepository repository, UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
    }

    /** Valida o valor recebido em X-API-Key. Lança {@link ApiKeyInvalidaException} se não servir. */
    @Transactional(readOnly = true)
    public Credencial autenticar(String chave) {
        ApiKey apiKey = repository.findByChaveHash(HashUtil.sha256Hex(chave))
                .orElseThrow(() -> new ApiKeyInvalidaException("Chave de API inválida"));
        if (!apiKey.estaValida()) {
            throw new ApiKeyInvalidaException("Chave de API revogada ou expirada");
        }
        return new Credencial(apiKey.getId(), apiKey.getNivel());
    }

    @Transactional
    public ApiKeyCriadaResponseDTO criar(ApiKeyRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.usuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", dto.usuarioId()));

        byte[] aleatorio = new byte[32];
        RANDOM.nextBytes(aleatorio);
        String chave = PREFIXO_CHAVE + Base64.getUrlEncoder().withoutPadding().encodeToString(aleatorio);

        ApiKey apiKey = new ApiKey();
        apiKey.setPrefixo(chave.substring(0, TAMANHO_PREFIXO_EXIBIDO));
        apiKey.setChaveHash(HashUtil.sha256Hex(chave));
        apiKey.setDescricao(dto.descricao());
        apiKey.setNivel(dto.nivel());
        apiKey.setAtiva(true);
        apiKey.setCriadaEm(LocalDateTime.now());
        apiKey.setExpiraEm(dto.expiraEm());
        apiKey.setUsuario(usuario);
        ApiKey salva = repository.save(apiKey);

        return new ApiKeyCriadaResponseDTO(salva.getId(), chave, salva.getPrefixo(), salva.getDescricao(),
                salva.getNivel(), salva.getAtiva(), salva.getCriadaEm(), salva.getExpiraEm(), usuario.getId());
    }

    @Transactional(readOnly = true)
    public Page<ApiKeyResponseDTO> listar(Pageable pageable) {
        return repository.findAll(pageable).map(ApiKeyService::toResponse);
    }

    @Transactional(readOnly = true)
    public ApiKeyResponseDTO buscarPorId(Long id) {
        return toResponse(obter(id));
    }

    @Transactional
    public ApiKeyResponseDTO atualizar(Long id, ApiKeyUpdateDTO dto) {
        ApiKey apiKey = obter(id);
        apiKey.setDescricao(dto.descricao());
        apiKey.setNivel(dto.nivel());
        apiKey.setAtiva(dto.ativa());
        apiKey.setExpiraEm(dto.expiraEm());
        return toResponse(repository.save(apiKey));
    }

    /** DELETE revoga a chave (ela continua listada, mas com {@code ativa=false}) — preserva o histórico. */
    @Transactional
    public void revogar(Long id) {
        ApiKey apiKey = obter(id);
        apiKey.setAtiva(false);
        repository.save(apiKey);
    }

    @Transactional(readOnly = true)
    public Page<ApiKeyResponseDTO> listarPorUsuario(Long usuarioId, Pageable pageable) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException("Usuário", usuarioId);
        }
        return repository.findByUsuarioId(usuarioId, pageable).map(ApiKeyService::toResponse);
    }

    private ApiKey obter(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chave de API", id));
    }

    private static ApiKeyResponseDTO toResponse(ApiKey k) {
        return new ApiKeyResponseDTO(k.getId(), k.getPrefixo(), k.getDescricao(), k.getNivel(), k.getAtiva(),
                k.getCriadaEm(), k.getExpiraEm(), k.getUsuario().getId());
    }
}
