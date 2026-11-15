package com.taskflow.observer;

@FunctionalInterface
public interface Observador {
    /** Recebe, de forma síncrona, o estado consolidado após uma alteração. */
    void atualizar(EventoTarefa evento);
}
