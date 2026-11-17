package com.exemplo.bibliotecafilmes.repository;

import com.exemplo.bibliotecafilmes.entity.ApiKey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    Optional<ApiKey> findByChaveHash(String chaveHash);

    Page<ApiKey> findByUsuarioId(Long usuarioId, Pageable pageable);

    boolean existsByUsuarioId(Long usuarioId);
}
