package com.distribuidoras.inventario.application.usecase;

import com.distribuidoras.inventario.domain.model.Lote;
import com.distribuidoras.inventario.domain.repository.LoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConsultarLotesCriticosUseCase {
    
    private final LoteRepository loteRepository;

    public ConsultarLotesCriticosUseCase(LoteRepository loteRepository) {
        this.loteRepository = loteRepository;
    }

    public List<Lote> ejecutar(int diasAviso) {
        return loteRepository.findLotesCriticos(diasAviso);
    }
}
