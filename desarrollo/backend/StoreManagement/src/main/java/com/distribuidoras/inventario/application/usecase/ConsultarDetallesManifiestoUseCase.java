package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.exception.ManifiestoNotFoundException;
import com.distribuidoras.inventario.domain.model.DetalleManifiesto;
import com.distribuidoras.inventario.domain.model.Manifiesto;
import com.distribuidoras.inventario.domain.model.Producto;
import com.distribuidoras.inventario.domain.repository.DetalleManifiestoRepository;
import com.distribuidoras.inventario.domain.repository.ManifiestoRepository;
import com.distribuidoras.inventario.domain.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Caso de uso: Consultar Detalles de un Manifiesto.
 */
@Service
public class ConsultarDetallesManifiestoUseCase {

    private final ManifiestoRepository manifiestoRepository;
    private final DetalleManifiestoRepository detalleManifiestoRepository;
    private final ProductoRepository productoRepository;

    public ConsultarDetallesManifiestoUseCase(ManifiestoRepository manifiestoRepository,
                                              DetalleManifiestoRepository detalleManifiestoRepository,
                                              ProductoRepository productoRepository) {
        this.manifiestoRepository = manifiestoRepository;
        this.detalleManifiestoRepository = detalleManifiestoRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public ManifiestoDetalleResult ejecutar(UUID manifiestoId) {
        Manifiesto manifiesto = manifiestoRepository.findById(manifiestoId)
                .orElseThrow(() -> new ManifiestoNotFoundException(manifiestoId));

        List<DetalleManifiesto> detalles = detalleManifiestoRepository.findByManifiestoId(manifiestoId);

        List<LineaManifiestoResult> lineas = detalles.stream().map(d -> {
            Producto producto = productoRepository.findById(d.getSkuId()).orElse(null);
            String marca = producto != null ? producto.getMarca() : "N/A";
            String presentacion = producto != null ? producto.getPresentacion() : "N/A";
            return new LineaManifiestoResult(d.getDetalleId(), d.getSkuId(),
                    marca, presentacion, d.getCantidadEsperada(), d.getCantidadRecibida());
        }).toList();

        return new ManifiestoDetalleResult(manifiesto.getManifiestoId(),
                manifiesto.getNumeroManifiesto(), lineas);
    }

    public record ManifiestoDetalleResult(UUID manifiestoId, String numeroManifiesto,
                                           List<LineaManifiestoResult> lineas) {}

    public record LineaManifiestoResult(UUID detalleId, UUID skuId, String marca,
                                         String presentacion, int cantidadEsperada, int cantidadRecibida) {}
}
