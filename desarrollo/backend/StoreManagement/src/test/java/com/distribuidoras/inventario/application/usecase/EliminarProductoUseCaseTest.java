package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.exception.ProductoConLotesActivosException;
import com.distribuidoras.inventario.domain.exception.ProductoNotFoundException;
import com.distribuidoras.inventario.domain.model.Producto;
import com.distribuidoras.inventario.domain.repository.LoteRepository;
import com.distribuidoras.inventario.domain.repository.MovimientoInventarioRepository;
import com.distribuidoras.inventario.domain.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para EliminarProductoUseCase.
 * Formato skuId: SKU-001, SKU-012, SKU-111, etc.
 * Incluye validación de lotes activos (FR-011).
 */
@ExtendWith(MockitoExtension.class)
class EliminarProductoUseCaseTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private MovimientoInventarioRepository movimientoRepository;

    private EliminarProductoUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new EliminarProductoUseCase(productoRepository, loteRepository, movimientoRepository);
    }

    @Test
    @DisplayName("Eliminar producto exitoso (sin lotes activos)")
    void eliminarProducto_exitoso() {
        String skuId = "SKU-001";
        Producto producto = Producto.builder()
                .skuId(skuId)
                .marca("Pilsen")
                .presentacion("Six-pack")
                .contenidoMl(330)
                .pesoLogisticoKg(new BigDecimal("2.5"))
                .creadoEl(LocalDateTime.now())
                .build();
        when(productoRepository.findById(skuId)).thenReturn(Optional.of(producto));
        when(loteRepository.findBySkuIdWithStock(skuId)).thenReturn(List.of());
        when(loteRepository.findBySkuIdOrderByFechaVencimientoAsc(skuId)).thenReturn(List.of());
        when(movimientoRepository.countByFilters(skuId, null, null, null, null)).thenReturn(0L);

        useCase.ejecutar(skuId);

        verify(productoRepository).save(producto);
        assertThat(producto.isActivo()).isFalse();
    }

    @Test
    @DisplayName("Eliminar producto no existente - lanza excepción")
    void eliminarProducto_noExiste_lanzaExcepcion() {
        String skuId = "SKU-001";
        when(productoRepository.findById(skuId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.ejecutar(skuId))
                .isInstanceOf(ProductoNotFoundException.class);

        verify(productoRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Eliminar producto con lotes activos - lanza excepción (FR-011)")
    void eliminarProducto_conLotesActivos_lanzaExcepcion() {
        String skuId = "SKU-001";
        Producto producto = Producto.builder()
                .skuId(skuId)
                .marca("Pilsen")
                .presentacion("Six-pack")
                .contenidoMl(330)
                .pesoLogisticoKg(new BigDecimal("2.5"))
                .creadoEl(LocalDateTime.now())
                .build();
        when(productoRepository.findById(skuId)).thenReturn(Optional.of(producto));
        when(loteRepository.findBySkuIdWithStock(skuId)).thenReturn(List.of(mock(com.distribuidoras.inventario.domain.model.Lote.class)));

        assertThatThrownBy(() -> useCase.ejecutar(skuId))
                .isInstanceOf(ProductoConLotesActivosException.class);

        verify(productoRepository, never()).deleteById(any());
    }
}
