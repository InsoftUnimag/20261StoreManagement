package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.exception.ProductoNotFoundException;
import com.distribuidoras.inventario.domain.exception.StockInsuficienteException;
import com.distribuidoras.inventario.domain.model.*;
import com.distribuidoras.inventario.domain.model.enums.*;
import com.distribuidoras.inventario.domain.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    private RegistrarExcepcionUseCase useCase;

    private final UUID skuId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private final String codigoLote = "LOT-001";
    private final UUID operarioId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new RegistrarExcepcionUseCase(excepcionRepository, loteRepository,
                movimientoRepository, productoRepository);
    }

    @Test
    @DisplayName("Excepción tipo Avería reduce stock y crea movimiento")
    void excepcion_averia_exitosa() {
        Producto producto = new Producto(skuId, "Pilsen", "Six-pack", 330,
                new BigDecimal("2.5"), LocalDateTime.now());
        when(productoRepository.findById(skuId)).thenReturn(Optional.of(producto));

        Lote lote = new Lote(codigoLote, skuId, 240, LocalDate.now().plusMonths(6),
                null, true, false, new BigDecimal("2.5"), UUID.randomUUID(), LocalDateTime.now());
        when(loteRepository.findById(codigoLote)).thenReturn(Optional.of(lote));
        when(loteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(excepcionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(movimientoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var command = new RegistrarExcepcionUseCase.ExcepcionCommand(
                TipoExcepcion.AVERIA, skuId, codigoLote, 12,
                "Cajas dañadas por humedad", null, operarioId);

        var result = useCase.ejecutar(command);

        assertNotNull(result.excepcionId());
        assertEquals("AVERIA", result.tipoExcepcion());
        assertNotNull(result.movimientoGenerado());
        assertEquals(-12, result.movimientoGenerado().cantidad());

        // Verificar que el stock se redujo
        assertEquals(228, lote.getCantidad());

        verify(loteRepository).save(any());
        verify(movimientoRepository).save(any());
    }

    @Test
    @DisplayName("Excepción tipo Vencimiento fuerza stock a cero")
    void excepcion_vencimiento_fuerzaCero() {
        Producto producto = new Producto(skuId, "Pilsen", "Six-pack", 330,
                new BigDecimal("2.5"), LocalDateTime.now());
        when(productoRepository.findById(skuId)).thenReturn(Optional.of(producto));

        Lote lote = new Lote(codigoLote, skuId, 50, LocalDate.now().minusDays(1),
                null, true, false, new BigDecimal("2.5"), UUID.randomUUID(), LocalDateTime.now());
        when(loteRepository.findById(codigoLote)).thenReturn(Optional.of(lote));
        when(loteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(excepcionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(movimientoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var command = new RegistrarExcepcionUseCase.ExcepcionCommand(
                TipoExcepcion.VENCIMIENTO, skuId, codigoLote, 1, // cantidad no importa para vencimiento
                "Lote vencido detectado", null, operarioId);

        var result = useCase.ejecutar(command);

        assertEquals(0, lote.getCantidad());
        assertEquals(-50, result.movimientoGenerado().cantidad());
    }

    @Test
    @DisplayName("Stock insuficiente lanza StockInsuficienteException")
    void excepcion_stockInsuficiente() {
        Producto producto = new Producto(skuId, "Pilsen", "Six-pack", 330,
                new BigDecimal("2.5"), LocalDateTime.now());
        when(productoRepository.findById(skuId)).thenReturn(Optional.of(producto));

        Lote lote = new Lote(codigoLote, skuId, 10, LocalDate.now().plusMonths(6),
                null, true, false, new BigDecimal("2.5"), UUID.randomUUID(), LocalDateTime.now());
        when(loteRepository.findById(codigoLote)).thenReturn(Optional.of(lote));

        var command = new RegistrarExcepcionUseCase.ExcepcionCommand(
                TipoExcepcion.AVERIA, skuId, codigoLote, 50, // > 10 disponibles
                "Cajas dañadas", null, operarioId);

        assertThrows(StockInsuficienteException.class, () -> useCase.ejecutar(command));
    }

    @Test
    @DisplayName("SKU inexistente lanza ProductoNotFoundException")
    void excepcion_skuNoExiste() {
        when(productoRepository.findById(skuId)).thenReturn(Optional.empty());

        var command = new RegistrarExcepcionUseCase.ExcepcionCommand(
                TipoExcepcion.AVERIA, skuId, null, 12, "Daño", null, operarioId);

        assertThrows(ProductoNotFoundException.class, () -> useCase.ejecutar(command));
    }
}
