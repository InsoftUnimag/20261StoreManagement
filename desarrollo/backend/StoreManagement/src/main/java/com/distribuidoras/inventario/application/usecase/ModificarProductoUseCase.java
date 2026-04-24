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

        Producto producto = productoRepository.findById(skuId)
                .orElseThrow(() -> new ProductoNotFoundException(skuId));

        // Normalizamos como en Producto.crear()
        String marcaNorm = marca != null ? marca.trim().toUpperCase() : null;
        String presNorm = presentacion != null ? presentacion.trim().toUpperCase() : null;

        String nuevaMarca = marcaNorm != null ? marcaNorm : producto.getMarca();
        String nuevaPresentacion = presNorm != null ? presNorm : producto.getPresentacion();

        // Validar duplicado
        if (!nuevaMarca.equals(producto.getMarca()) || !nuevaPresentacion.equals(producto.getPresentacion())) {
            if (productoRepository.existsByMarcaAndPresentacionAndSkuIdNot(nuevaMarca, nuevaPresentacion, skuId)) {
                throw new ProductoDuplicadoException(nuevaMarca, nuevaPresentacion);
            }
        }

        // Validaciones básicas
        if (contenidoMl != null && contenidoMl <= 0) throw new IllegalArgumentException("contenidoMl > 0");
        if (pesoLogisticoKg != null && pesoLogisticoKg.compareTo(BigDecimal.ZERO) <= 0) 
            throw new IllegalArgumentException("pesoLogisticoKg > 0");

        List<BitacoraProducto> cambios = new ArrayList<>();
        LocalDateTime ahora = LocalDateTime.now();
        boolean pesoModificado = false;

        if (marcaNorm != null && !marcaNorm.equals(producto.getMarca())) {
            cambios.add(crearEntradaBitacora(skuId, "marca", producto.getMarca(), marcaNorm, descripcion, ahora));
        }

        if (presNorm != null && !presNorm.equals(producto.getPresentacion())) {
            cambios.add(crearEntradaBitacora(skuId, "presentacion", producto.getPresentacion(), presNorm, descripcion, ahora));
        }

        if (contenidoMl != null && !contenidoMl.equals(producto.getContenidoMl())) {
            cambios.add(crearEntradaBitacora(skuId, "contenido_ml",
                    String.valueOf(producto.getContenidoMl()), String.valueOf(contenidoMl), descripcion, ahora));
        }

        if (pesoLogisticoKg != null && pesoLogisticoKg.compareTo(producto.getPesoLogisticoKg()) != 0) {
            cambios.add(crearEntradaBitacora(skuId, "peso_logistico_kg",
                    producto.getPesoLogisticoKg().toString(), pesoLogisticoKg.toString(), descripcion, ahora));
            pesoModificado = true;
        }

        // Construimos NUEVO objeto inmutable en vez de hacer set
        Producto actualizado = Producto.builder()
                .skuId(producto.getSkuId())
                .marca(nuevaMarca)
                .presentacion(nuevaPresentacion)
                .contenidoMl(contenidoMl != null ? contenidoMl : producto.getContenidoMl())
                .pesoLogisticoKg(pesoLogisticoKg != null ? pesoLogisticoKg : producto.getPesoLogisticoKg())
                .creadoEl(producto.getCreadoEl())
                .build();

        cambios.forEach(bitacoraRepository::save);
        Producto guardado = productoRepository.save(actualizado);

        log.info("Producto modificado: SKU={}, campos cambiados={}", skuId, cambios.size());

        String alerta = null;
        if (pesoModificado) {
            alerta = "El peso logístico fue modificado. Los cálculos de capacidad de flota para rutas no despachadas podrían variar.";
            log.warn("ALERTA: Peso logístico modificado para SKU={}. {}", skuId, alerta);
        }

        return new ResultadoModificacion(guardado, alerta, cambios);
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

    public record ResultadoModificacion(
            Producto producto,
            String alerta,
            List<BitacoraProducto> cambios
    ) {}
}