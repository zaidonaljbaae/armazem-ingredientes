package com.vega.armazem.exception;

/** Ingrediente ou compartimento inexistente: vira 404. */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String message) {
        super(message);
    }
}
