package com.distribuidoras.inventario.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Producto {

    @EqualsAndHashCode.Include
    private final String skuId;
    private final String marca;
    private final String presentacion;
    private final Integer contenidoMl;
    private final BigDecimal pesoLogisticoKg;
    private final LocalDateTime creadoEl;

    public static Producto crear(String skuId, String marca, String presentacion,
                                 Integer contenidoMl, BigDecimal pesoLogisticoKg) {
        
        String m = Objects.requireNonNull(marca, "marca obligatoria").trim().toUpperCase();
        String p = Objects.requireNonNull(presentacion, "presentacion obligatoria").trim().toUpperCase();
        
        if (m.isEmpty() || p.isEmpty()) throw new IllegalArgumentException("marca/presentacion vacías");
        if (contenidoMl == null || contenidoMl <= 0) throw new IllegalArgumentException("contenidoMl > 0");
        if (pesoLogisticoKg == null || pesoLogisticoKg.compareTo(BigDecimal.ZERO) <= 0) 
            throw new IllegalArgumentException("pesoLogisticoKg > 0");

        return Producto.builder()
                .skuId(Objects.requireNonNull(skuId))
                .marca(m)
                .presentacion(p)
                .contenidoMl(contenidoMl)
                .pesoLogisticoKg(pesoLogisticoKg)
                .creadoEl(LocalDateTime.now())
                .build();
    }
}