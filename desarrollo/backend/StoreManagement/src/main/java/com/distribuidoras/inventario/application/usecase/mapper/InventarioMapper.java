package com.distribuidoras.inventario.application.usecase.mapper;

import com.distribuidoras.inventario.domain.model.Lote;
import com.distribuidoras.inventario.domain.model.Producto;
import com.distribuidoras.inventario.infrastructure.web.dto.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;

/**
 * Functional mappers for converting domain models to DTOs.
 * Formato skuId: SKU-001, SKU-012, SKU-111, etc.
 * Uses function composition and builder pattern.
 */
public final class InventarioMapper {

    private InventarioMapper() {
        // Utility class
    }

    // Functional mapper: Lote -> LoteStockDTO
    public static Function<Lote, LoteStockDTO> toLoteStockDTO() {
        return lote -> {
            int diasHastaVencimiento = calcularDiasHastaVencimiento(lote.getFechaVencimiento());
            boolean urgente = esLoteUrgente(lote.getFechaVencimiento(), 30); // 30 days threshold
            String estado = determinarEstadoLote(lote, diasHastaVencimiento);

            return LoteStockDTO.builder()
                    .codigoLote(lote.getCodigoLote())
                    .fechaVencimiento(lote.getFechaVencimiento())
                    .fechaExpedicion(lote.getFechaExpedicion())
                    .cantidad(lote.getCantidad())
                    .diasHastaVencimiento(diasHastaVencimiento)
                    .urgente(urgente)
                    .estado(estado)
                    .build();
        };
    }

    // Functional mapper: Producto -> ProductoInfo
    public static Function<Producto, StockDisponibleDTO.ProductoInfo> toProductoInfo() {
        return producto -> StockDisponibleDTO.ProductoInfo.builder()
                .skuId(producto.getSkuId())
                .marca(producto.getMarca())
                .presentacion(producto.getPresentacion())
                .contenidoMl(producto.getContenidoMl())
                .pesoLogisticoKg(
                        producto.getPesoLogisticoKg() != null ? producto.getPesoLogisticoKg().doubleValue() : null)
                .build();
    }

    // Functional mapper: Producto -> StockResumenDTO
    public static Function<Producto, StockResumenDTO> toStockResumenDTO(Integer stockTotal) {
        return producto -> StockResumenDTO.builder()
                .skuId(producto.getSkuId())
                .marca(producto.getMarca())
                .presentacion(producto.getPresentacion())
                .fisicoTotal(stockTotal)
                .build();
    }

    // Calculate days until expiration
    public static int calcularDiasHastaVencimiento(LocalDate fechaVencimiento) {
        if (fechaVencimiento == null) {
            return Integer.MAX_VALUE;
        }
        return (int) ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento);
    }

    // Check if lot is urgent (within threshold days)
    public static boolean esLoteUrgente(LocalDate fechaVencimiento, int umbralDias) {
        if (fechaVencimiento == null) {
            return false;
        }
        int dias = calcularDiasHastaVencimiento(fechaVencimiento);
        return dias <= umbralDias;
    }

    // Determine lot status
    private static String determinarEstadoLote(Lote lote, int diasHastaVencimiento) {
        if (lote.getCantidad() == null || lote.getCantidad() == 0) {
            return "Agotado";
        }
        if (diasHastaVencimiento < 0) {
            return "Vencido";
        }
        if (diasHastaVencimiento <= 7) {
            return "Crítico";
        }
        if (diasHastaVencimiento <= 30) {
            return "Próximo a vencer";
        }
        return "Disponible";
    }
}
