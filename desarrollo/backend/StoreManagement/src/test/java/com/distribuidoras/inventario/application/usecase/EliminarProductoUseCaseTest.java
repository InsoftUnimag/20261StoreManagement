package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.exception.ProductoConLotesActivosException;
import com.distribuidoras.inventario.domain.exception.ProductoNotFoundException;
import com.distribuidoras.inventario.domain.model.Producto;
import com.distribuidoras.inventario.domain.repository.LoteRepository;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para EliminarProductoUseCase.
 * Incluye validación de lotes activos (FR-011).
 */
@ExtendWith(MockitoExtension.class)
class EliminarProductoUseCaseTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private LoteRepository loteRepository;

    private EliminarProductoUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new EliminarProductoUseCase(productoRepository, loteRepository);
    }

    @Test
    @DisplayName("Eliminar producto exitoso (sin lotes activos)")
    void eliminarProducto_exitoso() {
        UUID skuId = UUID.randomUUID();
        Producto producto = new Producto(skuId, "Pilsen", "Six-pack", 330,
                new BigDecimal("2.5"), LocalDateTime.now());
        when(productoRepository.findById(skuId)).thenReturn(Optional.of(producto));
        when(loteRepository.existsBySkuIdAndCantidadGreaterThan(skuId, 0)).thenReturn(false);

        useCase.ejecutar(skuId);

        verify(productoRepository).deleteById(skuId);
    }

    @Test
    @DisplayName("Eliminar producto no existente - lanza excepción")
    void eliminarProducto_noExiste_lanzaExcepcion() {
        UUID skuId = UUID.randomUUID();
        when(productoRepository.findById(skuId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.ejecutar(skuId))
                .isInstanceOf(ProductoNotFoundException.class);

        verify(productoRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Eliminar producto con lotes activos - lanza excepción (FR-011)")
    void eliminarProducto_conLotesActivos_lanzaExcepcion() {
        UUID skuId = UUID.randomUUID();
        Producto producto = new Producto(skuId, "Pilsen", "Six-pack", 330,
                new BigDecimal("2.5"), LocalDateTime.now());
        when(productoRepository.findById(skuId)).thenReturn(Optional.of(producto));
        when(loteRepository.existsBySkuIdAndCantidadGreaterThan(skuId, 0)).thenReturn(true);

        assertThatThrownBy(() -> useCase.ejecutar(skuId))
                .isInstanceOf(ProductoConLotesActivosException.class);

        verify(productoRepository, never()).deleteById(any());
    }
}
