package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.model.Lote;
import com.distribuidoras.inventario.domain.model.Producto;
import com.distribuidoras.inventario.domain.repository.LoteRepository;
import com.distribuidoras.inventario.domain.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Caso de uso: Consultar Catálogo de Productos.
 * Spec: 03_consultar_productos.md
 *
 * Retorna productos con su disponibilidad.
 * FR-049: Solo cuenta lotes en estado DISPONIBLE (stock_actual > 0).
 * FR-051: No incluye detalles internos de lotes.
 * SC-023: Stock = suma exacta de lotes disponibles.
 */
@Service
public class ConsultarCatalogoUseCase {

    private static final Logger log = LoggerFactory.getLogger(ConsultarCatalogoUseCase.class);

    private final ProductoRepository productoRepository;
    private final LoteRepository loteRepository;

    public ConsultarCatalogoUseCase(ProductoRepository productoRepository,
                                     LoteRepository loteRepository) {
        this.productoRepository = productoRepository;
        this.loteRepository = loteRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductoConDisponibilidad> ejecutar(String busqueda) {
        List<Producto> productos;

        if (busqueda != null && !busqueda.isBlank()) {
            productos = productoRepository.findByBusqueda(busqueda.trim());
            log.info("Consulta catálogo con filtro '{}': {} resultados", busqueda, productos.size());
        } else {
            productos = productoRepository.findAll();
            log.info("Consulta catálogo completo: {} productos", productos.size());
        }

        return productos.stream()
                .map(this::mapConDisponibilidad)
                .toList();
    }

    private ProductoConDisponibilidad mapConDisponibilidad(Producto producto) {
        // FR-049: Calcular stock basándose solo en lotes con stock_actual > 0
        List<Lote> lotesDisponibles = loteRepository.findBySkuIdWithStock(producto.getSkuId());
        int stockDisponible = lotesDisponibles.stream()
                .mapToInt(Lote::getCantidad)
                .sum();

        String disponibilidad = stockDisponible > 0 ? "Disponible" : "No disponible";

        return new ProductoConDisponibilidad(producto, stockDisponible, disponibilidad);
    }

    /**
     * DTO interno que envuelve Producto con su disponibilidad calculada.
     */
    public record ProductoConDisponibilidad(
            Producto producto,
            int stockDisponible,
            String disponibilidad
    ) {}
}
