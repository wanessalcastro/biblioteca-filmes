package com.exemplo.bibliotecafilmes.service;

import com.exemplo.bibliotecafilmes.entity.IdempotencyRecord;
import com.exemplo.bibliotecafilmes.repository.IdempotencyRecordRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/** Armazena e consulta as respostas das requisições idempotentes (TTL configurável). */
@Service
public class IdempotenciaService {

    private final IdempotencyRecordRepository repository;
    private final Duration ttl;

    public IdempotenciaService(IdempotencyRecordRepository repository,
                               @Value("${app.idempotency.ttl-hours:24}") long ttlHoras) {
        this.repository = repository;
        this.ttl = Duration.ofHours(ttlHoras);
    }

    /** Retorna o registro se existir e ainda estiver dentro do TTL; registros vencidos são descartados. */
    @Transactional
    public Optional<IdempotencyRecord> buscarValido(String chave) {
        Optional<IdempotencyRecord> registro = repository.findByChave(chave);
        if (registro.isPresent() && registro.get().getCriadoEm().isBefore(LocalDateTime.now().minus(ttl))) {
            repository.delete(registro.get());
            repository.flush();
            return Optional.empty();
        }
        return registro;
    }

    @Transactional
    public void salvar(IdempotencyRecord registro) {
        repository.saveAndFlush(registro);
        // limpeza oportunista: em ~5% das gravações remove o que já venceu
        if (ThreadLocalRandom.current().nextInt(20) == 0) {
            repository.deleteByCriadoEmBefore(LocalDateTime.now().minus(ttl));
        }
    }
}
