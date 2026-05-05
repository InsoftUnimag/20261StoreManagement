package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.exception.ClienteInactivoException;
import com.distribuidoras.inventario.domain.exception.ClienteNotFoundException;
import com.distribuidoras.inventario.domain.model.Cliente;
import com.distribuidoras.inventario.domain.repository.ClienteServicePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ConsultarClienteUseCase.
 * Spec 07: Consultar Datos de Cliente
 */
@ExtendWith(MockitoExtension.class)
class ConsultarClienteUseCaseTest {

    @Mock
    private ClienteServicePort clienteServicePort;

    @InjectMocks
    private ConsultarClienteUseCase useCase;

    private Cliente clienteActivo;
    private Cliente clienteInactivo;

    @BeforeEach
    void setUp() {
        clienteActivo = Cliente.builder()
                .cedula("1234567890")
                .nombre("Juan Pérez")
                .telefono("+57 300 123 4567")
                .email("juan@example.com")
                .direccion("Calle 123 #45-67, Bogotá")
                .activo(true)
                .build();

        clienteInactivo = Cliente.builder()
                .cedula("0987654321")
                .nombre("María López")
                .telefono("+57 300 987 6543")
                .activo(false)
                .build();
    }

    @Test
    @DisplayName("Happy Path: Cliente activo encontrado exitosamente")
    void ejecutar_clienteActivo_retornaCliente() {
        // Given
        when(clienteServicePort.findByCedula("1234567890")).thenReturn(Optional.of(clienteActivo));

        // When
        Cliente resultado = useCase.ejecutar("1234567890");

        // Then
        assertNotNull(resultado);
        assertEquals("1234567890", resultado.getCedula());
        assertEquals("Juan Pérez", resultado.getNombre());
        assertTrue(resultado.getActivo());
    }

    @Test
    @DisplayName("Error Case: Cliente no encontrado lanza ClienteNotFoundException")
    void ejecutar_clienteNoExiste_lanzaException() {
        // Given
        when(clienteServicePort.findByCedula("9999999999")).thenReturn(Optional.empty());

        // When / Then
        assertThrows(ClienteNotFoundException.class, () -> useCase.ejecutar("9999999999"));
    }

    @Test
    @DisplayName("Error Case: Cliente inactivo lanza ClienteInactivoException")
    void ejecutar_clienteInactivo_lanzaException() {
        // Given
        when(clienteServicePort.findByCedula("0987654321")).thenReturn(Optional.of(clienteInactivo));

        // When / Then
        assertThrows(ClienteInactivoException.class, () -> useCase.ejecutar("0987654321"));
    }
}
