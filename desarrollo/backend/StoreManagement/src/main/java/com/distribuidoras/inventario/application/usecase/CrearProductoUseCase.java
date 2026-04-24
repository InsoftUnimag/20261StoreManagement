package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.exception.ProductoDuplicadoException;
import com.distribuidoras.inventario.domain.model.Producto;
import com.distribuidoras.inventario.domain.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // 1. Normaliza una sola vez
        String marcaNorm = marca.trim().toUpperCase();
        String presNorm = presentacion.trim().toUpperCase();

        // 2. Valida duplicado con los valores normalizados
        if (productoRepository.existsByMarcaAndPresentacion(marcaNorm, presNorm)) {
            throw new ProductoDuplicadoException(marcaNorm, presNorm);
        }

        // 3. Genera SKU
        String siguienteSku = productoRepository.findMaxSkuNumero()
                .map(max -> String.format("SKU-%03d", max + 1))
                .orElse("SKU-001");

        // 4. Crea con los valores ya normalizados (Producto.crear los vuelve a
        // normalizar, no pasa nada)
        Producto producto = Producto.crear(siguienteSku, marcaNorm, presNorm, contenidoMl, pesoLogisticoKg);

        Producto guardado = productoRepository.save(producto);

        log.info("Producto creado exitosamente: SKU={}, marca={}, presentación={}",
                guardado.getSkuId(), guardado.getMarca(), guardado.getPresentacion());

        return guardado;
    }
}