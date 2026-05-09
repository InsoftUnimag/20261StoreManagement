package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.exception.ProductoNotFoundException;
import com.distribuidoras.inventario.domain.model.*;
import com.distribuidoras.inventario.domain.model.enums.*;
import com.distribuidoras.inventario.domain.repository.*;
import com.distribuidoras.inventario.infrastructure.persistence.repository.StockGlobalSkuJpaRepository;
import com.distribuidoras.inventario.infrastructure.persistence.entity.StockGlobalSkuJpaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrarExcepcionUseCaseTest {

    @Mock private ExcepcionInventarioRepository excepcionRepository;
    @Mock private LoteRepository loteRepository;
    @Mock private MovimientoInventarioRepository movimientoRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private StockGlobalSkuJpaRepository stockGlobalSkuRepository;
    @Mock private LoteComprometidoRepository loteComprometidoRepository;
    @Mock private RestTemplate restTemplate;

    private RegistrarExcepcionUseCase useCase;

    private final String skuId = "SKU-001";
    private final String codigoLote = "LOT-001";
    private final UUID operarioId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new RegistrarExcepcionUseCase(
                excepcionRepository, 
                loteRepository,
                movimientoRepository, 
                productoRepository, 
                stockGlobalSkuRepository, 
                loteComprometidoRepository,
                restTemplate);
    }

    @Test
    @DisplayName("Excepción tipo Avería reduce stock y crea movimiento")
    void excepcion_averia_exitosa() {
        Producto producto = Producto.builder()
                .skuId(skuId)
                .marca("Pilsen")
                .presentacion("Six-pack")
                .contenidoMl(330)
                .pesoLogisticoKg(new BigDecimal("2.5"))
                .creadoEl(LocalDateTime.now())
                .build();
        when(productoRepository.findById(skuId)).thenReturn(Optional.of(producto));

        Lote lote = Lote.builder()
                .codigoLote(codigoLote)
                .skuId(skuId)
                .cantidad(240)
                .fechaVencimiento(LocalDate.now().plusMonths(6))
                .disponible(true)
                .flagUrgenciaFefo(false)
                .build();
        when(loteRepository.findById(codigoLote)).thenReturn(Optional.of(lote));
        when(loteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(excepcionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(movimientoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        
        // Mock StockGlobalSku
        StockGlobalSkuJpaEntity stockEntity = new StockGlobalSkuJpaEntity();
        stockEntity.setFisicoTotal(500);
        stockEntity.setDisponibles(500);
        when(stockGlobalSkuRepository.findById(skuId)).thenReturn(Optional.of(stockEntity));

        var command = new RegistrarExcepcionUseCase.ExcepcionCommand(
                TipoExcepcion.AVERIA, skuId, codigoLote, 12,
                "Cajas dañadas por humedad", null, operarioId.toString());

        var result = useCase.ejecutar(command);

        assertNotNull(result.excepcionId());
        assertEquals("AVERIA", result.tipoExcepcion());
        assertNotNull(result.movimientoGenerado());
        assertEquals(-12, result.movimientoGenerado().cantidad());

        verify(loteRepository).save(any());
        verify(movimientoRepository).save(any());
        verify(stockGlobalSkuRepository).save(stockEntity);
    }

    @Test
    @DisplayName("Excepción tipo Vencimiento fuerza stock a cero")
    void excepcion_vencimiento_fuerzaCero() {
        Producto producto = Producto.builder()
                .skuId(skuId)
                .marca("Pilsen")
                .presentacion("Six-pack")
                .contenidoMl(330)
                .pesoLogisticoKg(new BigDecimal("2.5"))
                .creadoEl(LocalDateTime.now())
                .build();
        when(productoRepository.findById(skuId)).thenReturn(Optional.of(producto));

        Lote lote = Lote.builder()
                .codigoLote(codigoLote)
                .skuId(skuId)
                .cantidad(50)
                .fechaVencimiento(LocalDate.now().minusDays(1))
                .disponible(true)
                .build();
        when(loteRepository.findById(codigoLote)).thenReturn(Optional.of(lote));
        when(loteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(excepcionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(movimientoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        
        // Mock StockGlobalSku
        StockGlobalSkuJpaEntity stockEntityVenc = new StockGlobalSkuJpaEntity();
        stockEntityVenc.setFisicoTotal(100);
        stockEntityVenc.setDisponibles(100);
        when(stockGlobalSkuRepository.findById(skuId)).thenReturn(Optional.of(stockEntityVenc));

        var command = new RegistrarExcepcionUseCase.ExcepcionCommand(
                TipoExcepcion.VENCIMIENTO, skuId, codigoLote, 1,
                "Lote vencido detectado", null, operarioId.toString());

        var result = useCase.ejecutar(command);

        assertEquals(-50, result.movimientoGenerado().cantidad());
        verify(stockGlobalSkuRepository).save(stockEntityVenc);
    }

    @Test
    @DisplayName("Stock insuficiente lanza StockInsuficienteException")
    void excepcion_stockInsuficiente() {
        Producto producto = Producto.builder()
                .skuId(skuId)
                .marca("Pilsen")
                .presentacion("Six-pack")
                .contenidoMl(330)
                .build();
        when(productoRepository.findById(skuId)).thenReturn(Optional.of(producto));

        Lote lote = Lote.builder()
                .codigoLote(codigoLote)
                .skuId(skuId)
                .cantidad(10)
                .fechaVencimiento(LocalDate.now().plusMonths(6))
                .build();
        when(loteRepository.findById(codigoLote)).thenReturn(Optional.of(lote));

        var command = new RegistrarExcepcionUseCase.ExcepcionCommand(
                TipoExcepcion.AVERIA, skuId, codigoLote, 50,
                "Cajas dañadas", null, operarioId.toString());

        assertThrows(IllegalArgumentException.class, () -> useCase.ejecutar(command));
    }

    @Test
    @DisplayName("SKU inexistente lanza ProductoNotFoundException")
    void excepcion_skuNoExiste() {
        when(productoRepository.findById(skuId)).thenReturn(Optional.empty());

        var command = new RegistrarExcepcionUseCase.ExcepcionCommand(
                TipoExcepcion.AVERIA, skuId, null, 12, "Daño", null, operarioId.toString());

        assertThrows(ProductoNotFoundException.class, () -> useCase.ejecutar(command));
    }
}
