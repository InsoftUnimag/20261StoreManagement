package com.distribuidoras.inventario.infrastructure.web.dto;

/**
 * DTO representing pagination information.
 */
public record PaginacionDTO(
        Long totalElements,
        Integer totalPages,
        Integer currentPage,
        Integer pageSize
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long totalElements;
        private Integer totalPages;
        private Integer currentPage;
        private Integer pageSize;

        public Builder totalElements(Long totalElements) {
            this.totalElements = totalElements;
            return this;
        }

        public Builder totalPages(Integer totalPages) {
            this.totalPages = totalPages;
            return this;
        }

        public Builder currentPage(Integer currentPage) {
            this.currentPage = currentPage;
            return this;
        }

        public Builder pageSize(Integer pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public PaginacionDTO build() {
            return new PaginacionDTO(
                    totalElements,
                    totalPages,
                    currentPage,
                    pageSize
            );
        }
    }
}
