package com.distribuidoras.inventario.infrastructure.web.dto;

import java.util.List;

public record LoteDetalleDTO(
        LoteInfo lote,
        ProductoInfo producto,
        RecepcionInfo recepcion,
        List<MovimientoRecienteDTO> movimientosRecientes
) {
    public record LoteInfo(
            String codigoLote,
            String fechaVencimiento,
            String fechaFabricacion,
            int cantidadInicial,
            int cantidad,
            String creadoEl
    ) {}

    public record ProductoInfo(
            String skuId,
            String marca,
            String presentacion
    ) {}

    public record RecepcionInfo(
            String recepcionId,
            String fechaRecepcion,
            String operarioNombre
    ) {}

    public record MovimientoRecienteDTO(
            String movimientoId,
            String tipoMovimiento,
            int cantidad,
            String fecha,
            String pedidoId
    ) {}
}
