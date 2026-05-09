package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.exception.ProductoNotFoundException;
import com.distribuidoras.inventario.domain.model.Lote;
import com.distribuidoras.inventario.domain.model.Producto;
import com.distribuidoras.inventario.domain.repository.LoteRepository;
import com.distribuidoras.inventario.domain.repository.ProductoRepository;
import com.distribuidoras.inventario.infrastructure.persistence.repository.StockGlobalSkuJpaRepository;
import com.distribuidoras.inventario.infrastructure.web.dto.StockDisponibleDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class ConsultarStockPorSkuUseCaseTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private StockGlobalSkuJpaRepository stockGlobalSkuRepository;

    @InjectMocks
    private ConsultarStockPorSkuUseCase useCase;

    private String skuId;
    private Producto producto;
    private Lote lote1;
    private Lote lote2;

    @BeforeEach
    void setUp() {
        skuId = "SKU-001";
        lenient().when(stockGlobalSkuRepository.findById(Objects.requireNonNull(anyString())))
                .thenReturn(Optional.empty());

        producto = Producto.builder()
                .skuId(skuId)
                .marca("Pilsen")
                .presentacion("Six-pack")
                .contenidoMl(1980)
                .pesoLogisticoKg(new BigDecimal("2.5"))
                .creadoEl(LocalDateTime.now())
                .build();

        lote1 = Lote.builder()
                .codigoLote("LOT-2026-001")
                .skuId(skuId)
                .cantidad(240)
                .fechaVencimiento(LocalDate.now().plusDays(73))
                .fechaExpedicion(LocalDate.now().minusDays(30))
                .disponible(true)
                .flagUrgenciaFefo(false)
                .build();

        lote2 = Lote.builder()
                .codigoLote("LOT-2026-002")
                .skuId(skuId)
                .cantidad(180)
                .fechaVencimiento(LocalDate.now().plusDays(5))
                .fechaExpedicion(LocalDate.now().minusDays(20))
                .disponible(true)
                .flagUrgenciaFefo(true)
                .build();
    }

    @Test
    @DisplayName("Happy Path: SKU con múltiples lotes retorna lista ordenada FEFO")
    void ejecutar_conMultiplesLotes_retornaStockConFEFO() {
        when(productoRepository.findById(skuId)).thenReturn(Optional.of(producto));
        when(loteRepository.findBySkuIdWithStock(skuId))
                .thenReturn(List.of(lote2, lote1));

        StockDisponibleDTO resultado = useCase.ejecutar(skuId);

        assertNotNull(resultado);
        assertEquals(skuId, resultado.sku().skuId());
        assertEquals("Pilsen", resultado.sku().marca());
        assertEquals(420, resultado.fisicoTotal());

        assertEquals(2, resultado.lotes().size());
        assertEquals("LOT-2026-002", resultado.lotes().get(0).codigoLote());
        assertTrue(resultado.lotes().get(0).urgente());

        assertNotNull(resultado.proximoVencimiento());
        assertEquals("LOT-2026-002", resultado.proximoVencimiento().codigoLote());
        assertEquals(5, resultado.proximoVencimiento().diasRestantes());
    }

    @Test
    @DisplayName("Edge Case: SKU sin lotes retorna stock total = 0")
    void ejecutar_sinLotes_retornaStockCero() {
        when(productoRepository.findById(skuId)).thenReturn(Optional.of(producto));
        when(loteRepository.findBySkuIdWithStock(skuId)).thenReturn(List.of());

        StockDisponibleDTO resultado = useCase.ejecutar(skuId);

        assertNotNull(resultado);
        assertEquals(0, resultado.fisicoTotal());
        assertTrue(resultado.lotes().isEmpty());
        assertNull(resultado.proximoVencimiento());
    }

    @Test
    @DisplayName("Error Case: SKU inexistente lanza ProductoNotFoundException")
    void ejecutar_skuInexistente_lanzaException() {
        when(productoRepository.findById(skuId)).thenReturn(Optional.empty());

        assertThrows(ProductoNotFoundException.class, () -> useCase.ejecutar(skuId));
    }

    @Test
    @DisplayName("Functional: Calcula correctamente días hasta vencimiento")
    void ejecutar_calculaDiasHastaVencimiento() {
        LocalDate fechaVencimiento = LocalDate.now().plusDays(30);
        Lote lote = Lote.builder()
                .codigoLote("LOT-TEST")
                .skuId(skuId)
                .cantidad(100)
                .fechaVencimiento(fechaVencimiento)
                .build();

        when(productoRepository.findById(skuId)).thenReturn(Optional.of(producto));
        when(loteRepository.findBySkuIdWithStock(skuId)).thenReturn(List.of(lote));

        StockDisponibleDTO resultado = useCase.ejecutar(skuId);

        assertEquals(30, resultado.lotes().get(0).diasHastaVencimiento());
        assertFalse(resultado.lotes().get(0).urgente());
    }

    @Test
    @DisplayName("Functional: Lote crítico dentro de 7 días se marca como urgente")
    void ejecutar_loteCritico_marcaUrgente() {
        Lote loteCritico = Lote.builder()
                .codigoLote("LOT-CRITICO")
                .skuId(skuId)
                .cantidad(50)
                .fechaVencimiento(LocalDate.now().plusDays(3))
                .build();

        when(productoRepository.findById(skuId)).thenReturn(Optional.of(producto));
        when(loteRepository.findBySkuIdWithStock(skuId)).thenReturn(List.of(loteCritico));

        StockDisponibleDTO resultado = useCase.ejecutar(skuId);

        assertTrue(resultado.lotes().get(0).urgente());
        assertEquals("Crítico", resultado.lotes().get(0).estado());
    }
}