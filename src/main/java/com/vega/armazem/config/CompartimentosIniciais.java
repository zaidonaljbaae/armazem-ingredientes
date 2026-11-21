package com.vega.armazem.config;

import com.vega.armazem.service.EstoqueService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** Na primeira subida cria C1..C5, em ordem. Nas próximas não faz nada. */
@Component
public class CompartimentosIniciais implements ApplicationRunner {

    private final EstoqueService estoqueService;

    public CompartimentosIniciais(EstoqueService estoqueService) {
        this.estoqueService = estoqueService;
    }

    @Override
    public void run(ApplicationArguments args) {
        estoqueService.criarCompartimentosIniciais();
    }
}
