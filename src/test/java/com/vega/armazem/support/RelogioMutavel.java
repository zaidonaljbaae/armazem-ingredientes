package com.vega.armazem.support;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/** Relógio que o teste pode mudar, para simular "hoje" e "amanhã". */
public class RelogioMutavel extends Clock {

    private final ZoneId zona;
    private volatile Instant agora;

    public RelogioMutavel(ZoneId zona, LocalDateTime inicio) {
        this.zona = zona;
        this.agora = inicio.atZone(zona).toInstant();
    }

    public void definir(LocalDateTime dataHora) {
        this.agora = dataHora.atZone(zona).toInstant();
    }

    @Override
    public ZoneId getZone() {
        return zona;
    }

    @Override
    public Clock withZone(ZoneId outraZona) {
        return Clock.fixed(agora, outraZona);
    }

    @Override
    public Instant instant() {
        return agora;
    }
}
