package com.exemplo.bibliotecafilmes.repository;

import com.exemplo.bibliotecafilmes.entity.Diretor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiretorRepository extends JpaRepository<Diretor, Long> {

    Page<Diretor> findByNacionalidadeIgnoreCase(String nacionalidade, Pageable pageable);
}
