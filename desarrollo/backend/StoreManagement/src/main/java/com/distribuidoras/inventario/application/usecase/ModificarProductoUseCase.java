package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.exception.ProductoDuplicadoException;
import com.distribuidoras.inventario.domain.exception.ProductoNotFoundException;
import com.distribuidoras.inventario.domain.model.BitacoraProducto;
import com.distribuidoras.inventario.domain.model.Producto;
import com.distribuidoras.inventario.domain.repository.BitacoraProductoRepository;
import com.distribuidoras.inventario.domain.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Caso de uso: Modificar Producto existente.
 * Formato skuId: SKU-001, SKU-012, SKU-111, etc.
 * Spec: 02_modificar_plantilla_producto.md
 *
 * Actualiza atributos del producto, registra bitácora por cada cambio,
 * y genera alerta si se modifica peso logístico.
 */
@Service
public class ModificarProductoUseCase {

    private static final Logger log = LoggerFactory.getLogger(ModificarProductoUseCase.class);

    private final ProductoRepository productoRepository;
    private final BitacoraProductoRepository bitacoraRepository;

    public ModificarProductoUseCase(ProductoRepository productoRepository,
                                    BitacoraProductoRepository bitacoraRepository) {
        this.productoRepository = productoRepository;
        this.bitacoraRepository = bitacoraRepository;
    }

    @Transactional
    public ResultadoModificacion ejecutar(String skuId, String marca, String presentacion,
                                          Integer contenidoMl, BigDecimal pesoLogisticoKg,
                                          String descripcion) {

        // Cargar producto existente
        Producto producto = productoRepository.findById(skuId)
                .orElseThrow(() -> new ProductoNotFoundException(skuId));

        // FR-010: Validar que la modificación no genere duplicado
        String nuevaMarca = marca != null ? marca : producto.getMarca();
        String nuevaPresentacion = presentacion != null ? presentacion : producto.getPresentacion();

        if (!nuevaMarca.equals(producto.getMarca()) || !nuevaPresentacion.equals(producto.getPresentacion())) {
            if (productoRepository.existsByMarcaAndPresentacionAndSkuIdNot(nuevaMarca, nuevaPresentacion, skuId)) {
                throw new ProductoDuplicadoException(nuevaMarca, nuevaPresentacion);
            }
        }

        // FR-009: Registrar bitácora por cada campo modificado
        List<BitacoraProducto> cambios = new ArrayList<>();
        LocalDateTime ahora = LocalDateTime.now();
        boolean pesoModificado = false;

        if (marca != null && !marca.equals(producto.getMarca())) {
            cambios.add(crearEntradaBitacora(skuId, "marca",
                    producto.getMarca(), marca, descripcion, ahora));
            producto.setMarca(marca);
        }

        if (presentacion != null && !presentacion.equals(producto.getPresentacion())) {
            cambios.add(crearEntradaBitacora(skuId, "presentacion",
                    producto.getPresentacion(), presentacion, descripcion, ahora));
            producto.setPresentacion(presentacion);
        }

        if (contenidoMl != null && !contenidoMl.equals(producto.getContenidoMl())) {
            cambios.add(crearEntradaBitacora(skuId, "contenido_ml",
                    String.valueOf(producto.getContenidoMl()), String.valueOf(contenidoMl), descripcion, ahora));
            producto.setContenidoMl(contenidoMl);
        }

        if (pesoLogisticoKg != null && pesoLogisticoKg.compareTo(producto.getPesoLogisticoKg()) != 0) {
            cambios.add(crearEntradaBitacora(skuId, "peso_logistico_kg",
                    producto.getPesoLogisticoKg().toString(), pesoLogisticoKg.toString(), descripcion, ahora));
            producto.setPesoLogisticoKg(pesoLogisticoKg);
            pesoModificado = true;
        }

        // Guardar cambios en bitácora
        cambios.forEach(bitacoraRepository::save);

        // Guardar producto actualizado
        Producto actualizado = productoRepository.save(producto);

        log.info("Producto modificado: SKU={}, campos cambiados={}", skuId, cambios.size());

        // Generar alerta si se modificó peso logístico
        String alerta = null;
        if (pesoModificado) {
            alerta = "El peso logístico fue modificado. Los cálculos de capacidad de flota para rutas no despachadas podrían variar.";
            log.warn("ALERTA: Peso logístico modificado para SKU={}. {}", skuId, alerta);
        }

        return new ResultadoModificacion(actualizado, alerta, cambios);
    }

    private BitacoraProducto crearEntradaBitacora(String skuId, String campo,
                                                   String valorAnterior, String valorNuevo,
                                                   String descripcion, LocalDateTime fecha) {
        BitacoraProducto bitacora = new BitacoraProducto();
        bitacora.setSkuIdRef(skuId);
        bitacora.setCampo(campo);
        bitacora.setValorAnterior(valorAnterior);
        bitacora.setValorNuevo(valorNuevo);
        bitacora.setDescripcion(descripcion);
        bitacora.setFecha(fecha);
        return bitacora;
    }

    /**
     * Resultado de la operación de modificación.
     * Incluye el producto actualizado, una posible alerta y la lista de cambios.
     */
    public record ResultadoModificacion(
            Producto producto,
            String alerta,
            List<BitacoraProducto> cambios
    ) {}
}
