package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.exception.ProductoDuplicadoException;
import com.distribuidoras.inventario.domain.model.Producto;
import com.distribuidoras.inventario.domain.repository.ProductoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para CrearProductoUseCase.
 * Sin Spring, solo Mockito.
 */
@ExtendWith(MockitoExtension.class)
class CrearProductoUseCaseTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private CrearProductoUseCase useCase;

    @Test
    @DisplayName("Crear producto exitoso - genera SKU formato SKU-001 y guarda con stock 0")
    void crearProducto_exitoso() {
        // Given
        when(productoRepository.existsByMarcaAndPresentacion("PILSEN", "SIX-PACK")).thenReturn(false);
        when(productoRepository.findMaxSkuNumero()).thenReturn(java.util.Optional.of(0));
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Producto resultado = useCase.ejecutar("PILSEN", "SIX-PACK", 330, new BigDecimal("2.5"));

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getSkuId()).isNotNull();
        assertThat(resultado.getSkuId()).startsWith("SKU-");
        assertThat(resultado.getMarca()).isEqualTo("PILSEN");
        assertThat(resultado.getPresentacion()).isEqualTo("SIX-PACK");
        assertThat(resultado.getContenidoMl()).isEqualTo(330);
        assertThat(resultado.getPesoLogisticoKg()).isEqualByComparingTo(new BigDecimal("2.5"));
        assertThat(resultado.getCreadoEl()).isNotNull();

        // Verificar que se guardó
        ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
        verify(productoRepository).save(captor.capture());
        assertThat(captor.getValue().getSkuId()).isNotNull();
    }

    @Test
    @DisplayName("Crear producto duplicado - lanza ProductoDuplicadoException (SC-003)")
    void crearProducto_duplicado_lanzaExcepcion() {
        // Given
        when(productoRepository.existsByMarcaAndPresentacion("PILSEN", "SIX-PACK")).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> useCase.ejecutar("PILSEN", "SIX-PACK", 330, new BigDecimal("2.5")))
                .isInstanceOf(ProductoDuplicadoException.class)
                .hasMessageContaining("PILSEN")
                .hasMessageContaining("SIX-PACK");

        // Verificar que NO se intentó guardar
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Misma marca, diferente presentación - son productos distintos")
    void crearProducto_mismaMarca_diferentePresentacion_exitoso() {
        // Given - misma marca pero diferente presentación
        when(productoRepository.existsByMarcaAndPresentacion("PILSEN", "UNIDAD")).thenReturn(false);
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Producto resultado = useCase.ejecutar("PILSEN", "UNIDAD", 330, new BigDecimal("0.5"));

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getMarca()).isEqualTo("PILSEN");
        assertThat(resultado.getPresentacion()).isEqualTo("UNIDAD");
    }

    @Test
    @DisplayName("Crear producto duplicado - case insensitive en BD (AGUILA+SIXPACK ya existe)")
    void crearProducto_duplicado_caseInsensitive_lanzaExcepcion() {
        // Given - la query JPQL usa LOWER() para comparación case-insensitive
        when(productoRepository.existsByMarcaAndPresentacion("AGUILA", "SIXPACK")).thenReturn(true);

        // When / Then - debe lanzar excepción aunque la BD haga la comparación case-insensitive
        assertThatThrownBy(() -> useCase.ejecutar("AGUILA", "SIXPACK", 190, new BigDecimal("1.0")))
                .isInstanceOf(ProductoDuplicadoException.class);

        verify(productoRepository).existsByMarcaAndPresentacion("AGUILA", "SIXPACK");
    }
}
