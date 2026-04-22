package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.exception.ProductoDuplicadoException;
import com.distribuidoras.inventario.domain.model.Producto;
import com.distribuidoras.inventario.domain.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Caso de uso: Crear Producto (SKU).
 * Spec: 01_crear_plantilla_producto.md
 *
 * Valida que no exista duplicado por marca + presentación,
 * genera SKU único y guarda el producto con stock inicial = 0.
 */
@Service
public class CrearProductoUseCase {

    private static final Logger log = LoggerFactory.getLogger(CrearProductoUseCase.class);

    private final ProductoRepository productoRepository;

    public CrearProductoUseCase(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Transactional
    public Producto ejecutar(String marca, String presentacion, Integer contenidoMl,
                             java.math.BigDecimal pesoLogisticoKg) {

        // FR-005: Validar que no exista combinación marca + presentación
        if (productoRepository.existsByMarcaAndPresentacion(marca, presentacion)) {
            throw new ProductoDuplicadoException(marca, presentacion);
        }

        // FR-003: Generar SKU único automáticamente
        Producto producto = new Producto();
        producto.setSkuId(UUID.randomUUID());
        producto.setMarca(marca);
        producto.setPresentacion(presentacion);
        producto.setContenidoMl(contenidoMl);
        producto.setPesoLogisticoKg(pesoLogisticoKg);
        producto.setCreadoEl(LocalDateTime.now());

        Producto guardado = productoRepository.save(producto);

        log.info("Producto creado exitosamente: SKU={}, marca={}, presentación={}",
                guardado.getSkuId(), guardado.getMarca(), guardado.getPresentacion());

        return guardado;
    }
}
