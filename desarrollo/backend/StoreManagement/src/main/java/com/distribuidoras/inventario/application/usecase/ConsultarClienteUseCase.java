package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.exception.ClienteInactivoException;
import com.distribuidoras.inventario.domain.exception.ClienteNotFoundException;
import com.distribuidoras.inventario.domain.model.Cliente;
import com.distribuidoras.inventario.domain.repository.ClienteServicePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Use Case: Consultar datos de cliente por CC/NIT.
 * Spec 07: Consultar Datos de Cliente
 * 
 * FR-042: Consultar datos de cliente al módulo de usuarios externo
 * FR-043: Retornar nombre, CC/NIT, teléfono y estado
 * FR-044: Impedir vincular clientes inactivos
 * FR-045: Manejar error de cliente no encontrado
 * FR-046: Manejar error de conectividad
 */
@Service
public class ConsultarClienteUseCase {

    private static final Logger log = LoggerFactory.getLogger(ConsultarClienteUseCase.class);

    private final ClienteServicePort clienteServicePort;

    public ConsultarClienteUseCase(ClienteServicePort clienteServicePort) {
        this.clienteServicePort = clienteServicePort;
    }

    /**
     * Ejecuta la consulta de cliente por CC/NIT.
     * 
     * @param cedula CC/NIT del cliente
     * @return Cliente encontrado
     */
    public Cliente ejecutar(String cedula) {
        log.info("Consultando cliente con CC: {}", cedula);

        // Consultar módulo de usuarios
        Cliente cliente = clienteServicePort.findByCedula(cedula)
                .orElseThrow(() -> {
                    log.warn("Cliente no encontrado: {}", cedula);
                    return new ClienteNotFoundException(cedula);
                });

        // Validar que esté activo (FR-044)
        if (Boolean.FALSE.equals(cliente.getActivo())) {
            log.warn("Cliente inactivo: {} - {}", cedula, cliente.getNombre());
            throw new ClienteInactivoException(cedula);
        }

        log.info("Cliente encontrado y activo: {} - {}", cliente.getCedula(), cliente.getNombre());
        return cliente;
    }
}
