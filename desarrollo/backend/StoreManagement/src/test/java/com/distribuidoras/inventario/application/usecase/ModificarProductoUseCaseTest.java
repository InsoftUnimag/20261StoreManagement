package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.exception.ProductoDuplicadoException;
import com.distribuidoras.inventario.domain.exception.ProductoNotFoundException;
import com.distribuidoras.inventario.domain.model.BitacoraProducto;
import com.distribuidoras.inventario.domain.model.Producto;
import com.distribuidoras.inventario.domain.repository.BitacoraProductoRepository;
import com.distribuidoras.inventario.domain.repository.ProductoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ModificarProductoUseCase.
 * Sin Spring, solo Mockito.
 */
@ExtendWith(MockitoExtension.class)
class ModificarProductoUseCaseTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private BitacoraProductoRepository bitacoraRepository;

    @InjectMocks
    private ModificarProductoUseCase useCase;

    private Producto crearProductoExistente() {
        return new Producto(
                UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
                "Pilsen",
                "Six-pack",
                330,
                new BigDecimal("2.5"),
                LocalDateTime.of(2026, 4, 3, 19, 30)
        );
    }

    @Test
    @DisplayName("Modificar producto exitoso - actualiza atributo y registra bitácora (SC-004, SC-005)")
    void modificarProducto_exitoso() {
        // Given
        Producto existente = crearProductoExistente();
        UUID skuId = existente.getSkuId();

        when(productoRepository.findById(skuId)).thenReturn(Optional.of(existente));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bitacoraRepository.save(any(BitacoraProducto.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        ModificarProductoUseCase.ResultadoModificacion resultado = useCase.ejecutar(
                skuId, null, null, 500, null, "Cambio de contenido");

        // Then
        // SC-004: SKU permanece sin cambios
        assertThat(resultado.producto().getSkuId()).isEqualTo(skuId);
        assertThat(resultado.producto().getContenidoMl()).isEqualTo(500);
        assertThat(resultado.alerta()).isNull(); // No se modificó peso

        // SC-005: Bitácora registrada
        ArgumentCaptor<BitacoraProducto> captor = ArgumentCaptor.forClass(BitacoraProducto.class);
        verify(bitacoraRepository).save(captor.capture());
        BitacoraProducto bitacora = captor.getValue();
        assertThat(bitacora.getCampo()).isEqualTo("contenido_ml");
        assertThat(bitacora.getValorAnterior()).isEqualTo("330");
        assertThat(bitacora.getValorNuevo()).isEqualTo("500");
    }

    @Test
    @DisplayName("Modificar peso logístico - genera alerta (SC-006)")
    void modificarProducto_pesoLogistico_generaAlerta() {
        // Given
        Producto existente = crearProductoExistente();
        UUID skuId = existente.getSkuId();

        when(productoRepository.findById(skuId)).thenReturn(Optional.of(existente));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bitacoraRepository.save(any(BitacoraProducto.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        ModificarProductoUseCase.ResultadoModificacion resultado = useCase.ejecutar(
                skuId, null, null, null, new BigDecimal("2.8"), "Cambio de empaque");

        // Then
        assertThat(resultado.alerta()).isNotNull();
        assertThat(resultado.alerta()).contains("peso logístico fue modificado");
        assertThat(resultado.producto().getPesoLogisticoKg()).isEqualByComparingTo(new BigDecimal("2.8"));
    }

    @Test
    @DisplayName("Modificar producto no existente - lanza ProductoNotFoundException")
    void modificarProducto_noExiste_lanzaExcepcion() {
        // Given
        UUID skuId = UUID.randomUUID();
        when(productoRepository.findById(skuId)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> useCase.ejecutar(skuId, "Aguila", null, null, null, null))
                .isInstanceOf(ProductoNotFoundException.class);
    }

    @Test
    @DisplayName("Modificar marca genera duplicado - lanza ProductoDuplicadoException")
    void modificarProducto_duplicado_lanzaExcepcion() {
        // Given
        Producto existente = crearProductoExistente();
        UUID skuId = existente.getSkuId();

        when(productoRepository.findById(skuId)).thenReturn(Optional.of(existente));
        when(productoRepository.existsByMarcaAndPresentacionAndSkuIdNot("Aguila", "Six-pack", skuId))
                .thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> useCase.ejecutar(skuId, "Aguila", null, null, null, null))
                .isInstanceOf(ProductoDuplicadoException.class);
    }
}
