package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.model.Recepcion;
import com.distribuidoras.inventario.domain.repository.RecepcionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListarRecepcionesUseCase {

    private final RecepcionRepository recepcionRepository;

    public ListarRecepcionesUseCase(RecepcionRepository recepcionRepository) {
        this.recepcionRepository = recepcionRepository;
    }

    public List<Recepcion> ejecutar() {
        return recepcionRepository.findAll();
    }
}
