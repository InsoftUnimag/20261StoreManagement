package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.model.DetalleManifiesto;
import com.distribuidoras.inventario.domain.model.Manifiesto;
import com.distribuidoras.inventario.domain.model.Producto;
import com.distribuidoras.inventario.domain.model.enums.EstadoManifiesto;
import com.distribuidoras.inventario.domain.repository.ManifiestoRepository;
import com.distribuidoras.inventario.domain.repository.DetalleManifiestoRepository;
import com.distribuidoras.inventario.domain.repository.ProductoRepository;
import com.distribuidoras.inventario.infrastructure.web.dto.CrearManifiestoRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

import java.util.UUID;

@Service
public class CrearManifiestoUseCase {

    private static final Logger log = LoggerFactory.getLogger(CrearManifiestoUseCase.class);

    private final ManifiestoRepository manifistoRepository;
    private final DetalleManifiestoRepository detalleRepository;
    private final ProductoRepository productoRepository;

    public CrearManifiestoUseCase(
            ManifiestoRepository manifistoRepository,
            DetalleManifiestoRepository detalleRepository,
            ProductoRepository productoRepository) {
        this.manifistoRepository = manifistoRepository;
        this.detalleRepository = detalleRepository;
        this.productoRepository = productoRepository;
    }

    public Manifiesto ejecutar(CrearManifiestoRequest request) {
        log.info("Creando manifisto {}", request.getNumeroManifiesto());

        String numero = request.getNumeroManifiesto();
        if (numero == null || numero.isBlank()) {
            numero = generarNumeroManifiesto();
        }

        Manifiesto manifisto = Manifiesto.builder()
                .manifiestoId(UUID.randomUUID())
                .numeroManifiesto(numero)
                .fechaEmision(LocalDate.now())
                .proveedor(request.getProveedor())
                .estado(EstadoManifiesto.PENDIENTE)
                .build();

        manifisto = manifistoRepository.save(manifisto);

        if (request.getLineas() != null && !request.getLineas().isEmpty()) {
            for (CrearManifiestoRequest.LineaManifiestoRequest linea : request.getLineas()) {
                Producto producto = productoRepository.findById(linea.getSkuId()).orElse(null);
                if (producto != null) {
                    DetalleManifiesto detalle = DetalleManifiesto.builder()
                            .detalleId(UUID.randomUUID())
                            .manifistoId(manifisto.getManifiestoId())
                            .skuId(linea.getSkuId())
                            .cantidadEsperada(linea.getCantidadEsperada())
                            .cantidadRecibida(0)
                            .build();
                    detalleRepository.save(detalle);
                }
            }
        }

        log.info("Manifiesto {} creado exitosamente", numero);
        return manifisto;
    }

    private String generarNumeroManifiesto() {
        java.time.LocalDate hoy = java.time.LocalDate.now();
        String fecha = hoy.toString().replace("-", "");
        
        int siguienteNumero = manifistoRepository.findMaxNumeroManifiestoByFecha(hoy)
                .map(max -> max + 1)
                .orElse(1);
        
        return String.format("MAN-%s-%03d", fecha, siguienteNumero);
    }
}