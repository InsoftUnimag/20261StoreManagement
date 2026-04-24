package com.distribuidoras.inventario.infrastructure.persistence.repository;

import com.distribuidoras.inventario.infrastructure.persistence.entity.LoteComprometidoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LoteComprometidoJpaRepository extends JpaRepository<LoteComprometidoJpaEntity, UUID> {
    
    List<LoteComprometidoJpaEntity> findByProductoPedidoId(UUID productoPedidoId);

    List<LoteComprometidoJpaEntity> findByCodigoLote(String codigoLote);
}
