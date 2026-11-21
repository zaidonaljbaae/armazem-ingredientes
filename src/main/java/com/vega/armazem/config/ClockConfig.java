package com.vega.armazem.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/** Relógio injetável: os testes trocam por um relógio fixo para simular "amanhã". */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock(@Value("${armazem.fuso-horario}") String fusoHorario) {
        return Clock.system(ZoneId.of(fusoHorario));
    }
}
