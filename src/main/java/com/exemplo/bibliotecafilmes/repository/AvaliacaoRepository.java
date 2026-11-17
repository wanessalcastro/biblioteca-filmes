package com.exemplo.bibliotecafilmes.repository;

import com.exemplo.bibliotecafilmes.entity.Avaliacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    Page<Avaliacao> findByFilmeId(Long filmeId, Pageable pageable);

    Page<Avaliacao> findByUsuarioId(Long usuarioId, Pageable pageable);

    boolean existsByFilmeIdAndUsuarioId(Long filmeId, Long usuarioId);

    boolean existsByFilmeIdAndUsuarioIdAndIdNot(Long filmeId, Long usuarioId, Long id);

    boolean existsByUsuarioId(Long usuarioId);

    long countByFilmeId(Long filmeId);

    /** Média das notas do filme; {@code null} quando ainda não há avaliações. */
    @Query("select avg(a.nota) from Avaliacao a where a.filme.id = :filmeId")
    Double mediaNotaPorFilme(@Param("filmeId") Long filmeId);
}
