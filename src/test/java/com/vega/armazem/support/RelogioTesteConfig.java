package com.vega.armazem.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.LocalDateTime;
import java.time.ZoneId;

@TestConfiguration
public class RelogioTesteConfig {

    public static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    public static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 20, 10, 0);

    @Bean
    @Primary
    public RelogioMutavel relogioMutavel() {
        return new RelogioMutavel(ZONA, INICIO);
    }
}
