package com.distribuidoras.inventario.infrastructure.web.dto;

import java.util.List;

/**
 * DTO representing stock summary for multiple SKUs.
 * Used for batch stock queries.
 */
public record StockMultipleDTO(
        List<StockResumenDTO> stocks
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<StockResumenDTO> stocks;

        public Builder stocks(List<StockResumenDTO> stocks) {
            this.stocks = stocks;
            return this;
        }

        public StockMultipleDTO build() {
            return new StockMultipleDTO(stocks);
        }
    }
}
