package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.exception.ProductoNotFoundException;
import com.distribuidoras.inventario.domain.model.*;
import com.distribuidoras.inventario.domain.model.enums.*;
import com.distribuidoras.inventario.domain.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrarRecepcionUseCaseTest {

    @Mock private RecepcionRepository recepcionRepository;
    @Mock private LoteRepository loteRepository;
    @Mock private MovimientoInventarioRepository movimientoRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private ManifiestoRepository manifiestoRepository;
    @Mock private DetalleManifiestoRepository detalleManifiestoRepository;
    @Mock private ExcepcionInventarioRepository excepcionRepository;

    private RegistrarRecepcionUseCase useCase;

    private final UUID skuId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private final UUID operarioId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new RegistrarRecepcionUseCase(
                recepcionRepository, loteRepository, movimientoRepository,
                productoRepository, manifiestoRepository, detalleManifiestoRepository,
                excepcionRepository);
    }

    @Test
    @DisplayName("Recepción exitosa sin manifiesto crea lote y movimiento")
    void recepcionExitosa_sinManifiesto() {
        Producto producto = new Producto(skuId, "Pilsen", "Six-pack", 330,
                new BigDecimal("2.5"), LocalDateTime.now());
        when(productoRepository.findById(skuId)).thenReturn(Optional.of(producto));
        when(loteRepository.findBySkuIdAndCodigoLoteAndFechaVencimiento(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(recepcionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(loteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(movimientoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var command = new RegistrarRecepcionUseCase.RecepcionCommand(
                null, operarioId,
                List.of(new RegistrarRecepcionUseCase.LineaRecepcionCommand(
                        skuId, "LOT-001", LocalDate.now().plusMonths(6), null, 240)),
                "Sin novedades");

        var result = useCase.ejecutar(command);

        assertNotNull(result.recepcionId());
        assertEquals(1, result.lotesCreados().size());
        assertEquals(240, result.lotesCreados().get(0).cantidad());
        assertEquals(0, result.excepcionesGeneradas().size());

        verify(recepcionRepository).save(any());
        verify(loteRepository).save(any());
        verify(movimientoRepository).save(any());
    }

    @Test
    @DisplayName("SKU inexistente lanza ProductoNotFoundException")
    void recepcion_skuNoExiste() {
        when(productoRepository.findById(skuId)).thenReturn(Optional.empty());

        var command = new RegistrarRecepcionUseCase.RecepcionCommand(
                null, operarioId,
                List.of(new RegistrarRecepcionUseCase.LineaRecepcionCommand(
                        skuId, "LOT-001", LocalDate.now().plusMonths(6), null, 240)),
                null);

        assertThrows(ProductoNotFoundException.class, () -> useCase.ejecutar(command));
    }

    @Test
    @DisplayName("Fecha de vencimiento no futura lanza IllegalArgumentException")
    void recepcion_fechaVencimientoPasada() {
        Producto producto = new Producto(skuId, "Pilsen", "Six-pack", 330,
                new BigDecimal("2.5"), LocalDateTime.now());
        when(productoRepository.findById(skuId)).thenReturn(Optional.of(producto));

        var command = new RegistrarRecepcionUseCase.RecepcionCommand(
                null, operarioId,
                List.of(new RegistrarRecepcionUseCase.LineaRecepcionCommand(
                        skuId, "LOT-001", LocalDate.now(), null, 240)),
                null);

        assertThrows(IllegalArgumentException.class, () -> useCase.ejecutar(command));
    }

    @Test
    @DisplayName("Recepción con manifiesto y discrepancia genera excepción automática")
    void recepcion_conManifiesto_discrepancia() {
        UUID manifiestoId = UUID.randomUUID();
        Producto producto = new Producto(skuId, "Pilsen", "Six-pack", 330,
                new BigDecimal("2.5"), LocalDateTime.now());
        when(productoRepository.findById(skuId)).thenReturn(Optional.of(producto));
        when(loteRepository.findBySkuIdAndCodigoLoteAndFechaVencimiento(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(recepcionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(loteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(movimientoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(excepcionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Manifiesto espera 250, recibimos 240 → diferencia de -10
        DetalleManifiesto detalle = new DetalleManifiesto(UUID.randomUUID(), manifiestoId, skuId, 250, 0);
        when(detalleManifiestoRepository.findByManifiestoId(manifiestoId)).thenReturn(List.of(detalle));
        when(detalleManifiestoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(manifiestoRepository.findById(manifiestoId)).thenReturn(
                Optional.of(new Manifiesto(manifiestoId, "MAN-001", LocalDate.now(), "Bavaria", EstadoManifiesto.PENDIENTE)));
        when(manifiestoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var command = new RegistrarRecepcionUseCase.RecepcionCommand(
                manifiestoId, operarioId,
                List.of(new RegistrarRecepcionUseCase.LineaRecepcionCommand(
                        skuId, "LOT-001", LocalDate.now().plusMonths(6), null, 240)),
                null);

        var result = useCase.ejecutar(command);

        assertEquals(1, result.excepcionesGeneradas().size());
        assertTrue(result.excepcionesGeneradas().get(0).descripcion().contains("Esperado 250, Recibido 240"));

        verify(excepcionRepository).save(any());
    }
}
