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


@Service
public class ConsultarDetallesManifiestoUseCase {

    private final ManifiestoRepository repoManifiesto;
    private final DetalleManifiestoRepository repoDetalle;
    private final ProductoRepository repoProducto;

    public ConsultarDetallesManifiestoUseCase(ManifiestoRepository r1, DetalleManifiestoRepository r2, ProductoRepository r3) {
        this.repoManifiesto = r1;
        this.repoDetalle = r2;
        this.repoProducto = r3;
    }

    @Transactional(readOnly = true)
    public ManifiestoDetalleResult ejecutar(Long id) {
        Manifiesto m = repoManifiesto.findById(id).orElseThrow(() -> new ManifiestoNotFoundException(id));
        List<DetalleManifiesto> lista = repoDetalle.findByManifiestoId(id);
        List<LineaManifiestoResult> lineas = lista.stream().map(d -> {
            Producto p = repoProducto.findById(d.getSkuId()).orElse(null);
            return new LineaManifiestoResult(d.getDetalleId(), d.getSkuId(),
                    p != null ? p.getMarca() : "N/A",
                    p != null ? p.getPresentacion() : "N/A",
                    d.getCantidadEsperada(), d.getCantidadRecibida());
        }).toList();
        return new ManifiestoDetalleResult(m.getManifiestoId(), m.getNumeroManifiesto(), lineas);
    }

    public record ManifiestoDetalleResult(Long manifiestoId, String numeroManifiesto, List<LineaManifiestoResult> lineas) {}
    public record LineaManifiestoResult(Long detalleId, String skuId, String marca, String presentacion, int cantidadEsperada, int cantidadRecibida) {}
}