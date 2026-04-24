package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.model.Lote;
import com.distribuidoras.inventario.domain.model.Producto;
import com.distribuidoras.inventario.domain.repository.LoteRepository;
import com.distribuidoras.inventario.domain.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
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
    public Page<ProductoConDisponibilidad> ejecutarConPaginacion(String busqueda, Pageable pageable) {
        Page<Producto> productos;

        if (busqueda != null && !busqueda.isBlank()) {
            productos = productoRepository.findByBusquedaWithPagination(busqueda.trim(), pageable);
            log.info("Consulta catálogo (paginado) con filtro '{}': {} resultados en la página", busqueda, productos.getNumberOfElements());
        } else {
            productos = productoRepository.findAllWithPagination(pageable);
            log.info("Consulta catálogo completo (paginado): {} productos en la página", productos.getNumberOfElements());
        }

        return productos.map(this::mapConDisponibilidad);
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

        // GAP-07: Obtener costoCop del lote más reciente (último recibido)
        BigDecimal costoCop = lotesDisponibles.stream()
                .filter(l -> l.getCreadoEl() != null)
                .max(Comparator.comparing(Lote::getCreadoEl))
                .map(Lote::getCostoUnitarioProducto)
                .orElse(null);

        String disponibilidad = stockDisponible > 0 ? "Disponible" : "No disponible";

        return new ProductoConDisponibilidad(producto, stockDisponible, disponibilidad, costoCop);
    }

    /**
     * DTO interno que envuelve Producto con su disponibilidad calculada.
     */
    public record ProductoConDisponibilidad(
            Producto producto,
            int stockDisponible,
            String disponibilidad,
            BigDecimal costoCop
    ) {}
}
