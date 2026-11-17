package com.exemplo.bibliotecafilmes.repository;

import com.exemplo.bibliotecafilmes.entity.Genero;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GeneroRepository extends JpaRepository<Genero, Long> {

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    @Query(value = "select g from Genero g where g.id in "
            + "(select g2.id from Filme f join f.generos g2 where f.id = :filmeId)",
            countQuery = "select count(g) from Genero g where g.id in "
            + "(select g2.id from Filme f join f.generos g2 where f.id = :filmeId)")
    Page<Genero> findByFilmeId(@Param("filmeId") Long filmeId, Pageable pageable);
}
