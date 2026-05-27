package com.distribuidoras.inventario.infrastructure.persistence.adapter;

import com.distribuidoras.inventario.domain.model.Pedido;
import com.distribuidoras.inventario.domain.model.enums.EstadoPedido;
import com.distribuidoras.inventario.domain.repository.PedidoRepository;
import com.distribuidoras.inventario.infrastructure.persistence.entity.PedidoJpaEntity;
import com.distribuidoras.inventario.infrastructure.persistence.repository.PedidoJpaRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import java.util.Objects;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PedidoRepositoryAdapter implements PedidoRepository {

    private final PedidoJpaRepository jpa;

    public PedidoRepositoryAdapter(PedidoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Pedido save(Pedido pedido) {
        return toDomain(jpa.save(Objects.requireNonNull(toEntity(pedido))));
    }

    @Override
    public Optional<Pedido> findById(Long pedidoId) {
        return jpa.findById(Objects.requireNonNull(pedidoId)).map(this::toDomain);
    }

    @Override
    public Optional<Pedido> findByNumeroPedido(String numeroPedido) {
        return jpa.findByNumeroPedido(numeroPedido).map(this::toDomain);
    }

    @Override
    public String generarNumeroPedido(LocalDate fecha) {
        String prefix = "PED-" + fecha.toString().replace("-", "");
        Integer maxSeq = Optional.ofNullable(jpa.findMaxNumeroPedidoByPrefix(prefix)).orElse(0);
        return prefix + "-%03d".formatted(maxSeq + 1);
    }

    @Override
    public Page<Pedido> findByFilters(EstadoPedido estado, String clienteCc, String numeroPedido,
            LocalDate fechaDesde, LocalDate fechaHasta, @NonNull Pageable pageable) {

        Specification<PedidoJpaEntity> spec = (Root<PedidoJpaEntity> root,
                jakarta.persistence.criteria.CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (estado != null)
                predicates.add(cb.equal(root.get("estado"), estado));
            if (clienteCc != null)
                predicates.add(cb.like(root.get("clienteCc"), "%" + clienteCc + "%"));
            if (numeroPedido != null)
                predicates.add(cb.like(root.get("numeroPedido"), "%" + numeroPedido + "%"));
            if (fechaDesde != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaCreacion"), fechaDesde.atStartOfDay()));
            if (fechaHasta != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaCreacion"), fechaHasta.atTime(23, 59, 59)));

            // Forzamos el ordenamiento por fecha de creación descendente
            query.orderBy(cb.desc(root.get("fechaCreacion")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return jpa.findAll(spec, pageable).map(this::toDomain);
    }

    @Override
    public void update(Pedido pedido) {
        jpa.save(Objects.requireNonNull(toEntity(pedido)));
    }

    @Override
    public List<Pedido> findByEstadoOrderByFechaCreacionAsc(EstadoPedido estado) {
        return jpa.findByEstadoOrderByFechaCreacionAsc(estado).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Pedido> findByEstadoOrderByFechaCompromisoAsc(EstadoPedido estado) {
        return jpa.findByEstadoOrderByFechaCompromisoAsc(estado).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Pedido> findByEstadoWithPickingOrderByFechaPickingAsc(EstadoPedido estado) {
        // En esta fase 1 se omitirá el join con picking y ordenará temporalmente por
        // creación o compromiso
        return jpa.findByEstadoOrderByFechaCreacionAsc(estado).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Pedido> findByRutaId(Long rutaId) {
        return jpa.findByRutaId(rutaId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Pedido> findByOperarioPickingId(Long operarioPickingId) {
        return jpa.findByOperarioPickingId(operarioPickingId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Pedido> findByOperarioDespachoId(Long operarioDespachoId) {
        return jpa.findByOperarioDespachoId(operarioDespachoId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    // Functional mapper to Entity
    private PedidoJpaEntity toEntity(Pedido p) {
        return PedidoJpaEntity.builder()
                .pedidoId(p.getPedidoId())
                .numeroPedido(p.getNumeroPedido())
                .clienteCc(p.getClienteCc())
                .clienteNombre(p.getClienteNombre())
                .fechaCreacion(p.getFechaCreacion())
                .estado(p.getEstado())
                .rutaId(p.getRutaId())
                .fechaCompromiso(p.getFechaCompromiso())
                .asesorId(p.getAsesorId())
                .operarioPickingId(p.getOperarioPickingId())
                .operarioDespachoId(p.getOperarioDespachoId())
                .direccionEntrega(p.getDireccionEntrega())
                .fechaRecogida(p.getFechaRecogida())
                .costoTotal(p.getCostoTotal())
                .build();
    }

    // Functional mapper to Domain
    private Pedido toDomain(PedidoJpaEntity e) {
        return Pedido.builder()
                .pedidoId(e.getPedidoId())
                .numeroPedido(e.getNumeroPedido())
                .clienteCc(e.getClienteCc())
                .clienteNombre(e.getClienteNombre())
                .fechaCreacion(e.getFechaCreacion())
                .estado(e.getEstado())
                .rutaId(e.getRutaId())
                .fechaCompromiso(e.getFechaCompromiso())
                .asesorId(e.getAsesorId())
                .operarioPickingId(e.getOperarioPickingId())
                .operarioDespachoId(e.getOperarioDespachoId())
                .direccionEntrega(e.getDireccionEntrega())
                .fechaRecogida(e.getFechaRecogida())
                .build();
    }
}
