package com.exemplo.bibliotecafilmes.repository;

import com.exemplo.bibliotecafilmes.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    Page<Usuario> findByEmailContainingIgnoreCase(String email, Pageable pageable);
}
