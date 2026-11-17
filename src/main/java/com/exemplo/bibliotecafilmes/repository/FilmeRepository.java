package com.exemplo.bibliotecafilmes.repository;

import com.exemplo.bibliotecafilmes.entity.Filme;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FilmeRepository extends JpaRepository<Filme, Long> {

    Page<Filme> findByTituloContainingIgnoreCase(String titulo, Pageable pageable);

    Page<Filme> findByAnoLancamento(Integer ano, Pageable pageable);

    Page<Filme> findByGenerosId(Long generoId, Pageable pageable);

    Page<Filme> findByDiretorId(Long diretorId, Pageable pageable);

    boolean existsByDiretorId(Long diretorId);

    boolean existsByGenerosId(Long generoId);
}
