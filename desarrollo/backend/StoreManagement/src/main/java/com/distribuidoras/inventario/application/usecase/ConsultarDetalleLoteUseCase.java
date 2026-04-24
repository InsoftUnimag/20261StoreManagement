package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.model.Lote;
import com.distribuidoras.inventario.domain.repository.LoteRepository;
import com.distribuidoras.inventario.infrastructure.web.dto.LoteDetalleDTO;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class ConsultarDetalleLoteUseCase {

    private final LoteRepository loteRepository;

    public ConsultarDetalleLoteUseCase(LoteRepository loteRepository) {
        this.loteRepository = loteRepository;
    }

    public Optional<LoteDetalleDTO> ejecutar(String codigoLote) {
        Optional<Lote> loteOpt = loteRepository.findById(codigoLote);
        
        if (loteOpt.isEmpty()) {
            return Optional.empty();
        }
        
        Lote lote = loteOpt.get();
        // Basic mapping for now
        LoteDetalleDTO.LoteInfo loteInfo = new LoteDetalleDTO.LoteInfo(
                lote.getCodigoLote(),
                lote.getFechaVencimiento() != null ? lote.getFechaVencimiento().toString() : null,
                null, // No fechaFabricacion in Lote entity
                lote.getCantidad(), // Assuming cantidadInicial is same as current for this basic implementation or missing
                lote.getCantidad(),
                lote.getCreadoEl() != null ? lote.getCreadoEl().toString() : null
        );
        
        LoteDetalleDTO.ProductoInfo productoInfo = new LoteDetalleDTO.ProductoInfo(
                lote.getSkuId(),
                "Marca", // Placeholder
                "Presentacion" // Placeholder
        );
        
        LoteDetalleDTO.RecepcionInfo recepcionInfo = new LoteDetalleDTO.RecepcionInfo(
                lote.getRecepcionId() != null ? lote.getRecepcionId().toString() : null,
                "Fecha", // Placeholder
                "Operario" // Placeholder
        );

        return Optional.of(new LoteDetalleDTO(
                loteInfo,
                productoInfo,
                recepcionInfo,
                Collections.emptyList()
        ));
    }
}
