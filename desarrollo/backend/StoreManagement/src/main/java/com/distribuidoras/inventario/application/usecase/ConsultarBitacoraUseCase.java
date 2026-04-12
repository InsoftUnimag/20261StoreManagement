package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.exception.ProductoNotFoundException;
import com.distribuidoras.inventario.domain.model.BitacoraProducto;
import com.distribuidoras.inventario.domain.repository.BitacoraProductoRepository;
import com.distribuidoras.inventario.domain.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Caso de uso: Consultar Bitácora de cambios de un Producto.
 * Spec: 02_modificar_plantilla_producto.md (FR-009)
 */
@Service
public class ConsultarBitacoraUseCase {

    private static final Logger log = LoggerFactory.getLogger(ConsultarBitacoraUseCase.class);

    private final ProductoRepository productoRepository;
    private final BitacoraProductoRepository bitacoraRepository;

    public ConsultarBitacoraUseCase(ProductoRepository productoRepository,
                                    BitacoraProductoRepository bitacoraRepository) {
        this.productoRepository = productoRepository;
        this.bitacoraRepository = bitacoraRepository;
    }

    @Transactional(readOnly = true)
    public List<BitacoraProducto> ejecutar(UUID skuId) {
        // Verificar que el producto existe
        if (productoRepository.findById(skuId).isEmpty()) {
            throw new ProductoNotFoundException(skuId);
        }

        List<BitacoraProducto> historial = bitacoraRepository.findBySkuIdRef(skuId);

        log.info("Consulta bitácora SKU={}: {} registros", skuId, historial.size());

        return historial;
    }
}
