package com.vega.armazem.exception;

/** Regra de negócio violada ou parâmetro inválido: vira 400. */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String message) {
        super(message);
    }
}
