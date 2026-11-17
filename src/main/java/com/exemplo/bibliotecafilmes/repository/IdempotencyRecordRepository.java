package com.exemplo.bibliotecafilmes.repository;

import com.exemplo.bibliotecafilmes.entity.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, Long> {

    Optional<IdempotencyRecord> findByChave(String chave);

    void deleteByCriadoEmBefore(LocalDateTime limite);
}
