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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ModificarProductoUseCase.
 * Formato skuId: SKU-001, SKU-012, SKU-111, etc.
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
        return Producto.builder()
                .skuId("SKU-001")
                .marca("PILSEN")
                .presentacion("SIX-PACK")
                .contenidoMl(330)
                .pesoLogisticoKg(new BigDecimal("2.5"))
                .creadoEl(LocalDateTime.of(2026, 4, 3, 19, 30))
                .build();
    }

    @Test
    @DisplayName("Modificar producto exitoso - actualiza atributo y registra bitácora (SC-004, SC-005)")
    void modificarProducto_exitoso() {
        Producto existente = crearProductoExistente();
        String skuId = existente.getSkuId();

        when(productoRepository.findById(skuId)).thenReturn(Optional.of(existente));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bitacoraRepository.save(any(BitacoraProducto.class))).thenAnswer(inv -> inv.getArgument(0));

        ModificarProductoUseCase.ResultadoModificacion resultado = useCase.ejecutar(
                skuId, null, null, 500, null, "Cambio de contenido");

        assertThat(resultado.producto().getSkuId()).isEqualTo(skuId);
        assertThat(resultado.producto().getContenidoMl()).isEqualTo(500);
        assertThat(resultado.alerta()).isNull();

        ArgumentCaptor<BitacoraProducto> captor = ArgumentCaptor.forClass(BitacoraProducto.class);
        verify(bitacoraRepository).save(captor.capture());
        BitacoraProducto bitacora = captor.getValue();
        assertThat(bitacora.getCampo()).isEqualTo("contenido_ml");
        assertThat(bitacora.getValorAnterior()).isEqualTo("330");
        assertThat(bitacora.getValorNuevo()).isEqualTo("500");
    }

    @Test
    @DisplayName("Modificar peso logística - genera alerta (SC-006)")
    void modificarProducto_pesoLogistico_generaAlerta() {
        Producto existente = crearProductoExistente();
        String skuId = existente.getSkuId();

        when(productoRepository.findById(skuId)).thenReturn(Optional.of(existente));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bitacoraRepository.save(any(BitacoraProducto.class))).thenAnswer(inv -> inv.getArgument(0));

        ModificarProductoUseCase.ResultadoModificacion resultado = useCase.ejecutar(
                skuId, null, null, null, new BigDecimal("2.8"), "Cambio de empaque");

        assertThat(resultado.alerta()).isNotNull();
        assertThat(resultado.alerta()).contains("peso logístico fue modificado");
        assertThat(resultado.producto().getPesoLogisticoKg()).isEqualByComparingTo(new BigDecimal("2.8"));
    }

    @Test
    @DisplayName("Modificar producto no existente - lanza ProductoNotFoundException")
    void modificarProducto_noExiste_lanzaExcepcion() {
        String skuId = "SKU-001";
        when(productoRepository.findById(skuId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.ejecutar(skuId, "AGUILA", null, null, null, null))
                .isInstanceOf(ProductoNotFoundException.class);
    }

    @Test
    @DisplayName("Modificar marca genera duplicado - lanza ProductoDuplicadoException")
    void modificarProducto_duplicado_lanzaExcepcion() {
        Producto existente = crearProductoExistente();
        String skuId = existente.getSkuId();

        when(productoRepository.findById(skuId)).thenReturn(Optional.of(existente));
        when(productoRepository.existsByMarcaAndPresentacionAndSkuIdNot("AGUILA", "SIX-PACK", "SKU-001"))
                .thenReturn(true);

        assertThatThrownBy(() -> useCase.ejecutar(skuId, "AGUILA", null, null, null, null))
                .isInstanceOf(ProductoDuplicadoException.class);
    }
}
