package com.taskflow.model;

public enum Status {
    A_FAZER("A Fazer"),
    EM_ANDAMENTO("Em Andamento"),
    CONCLUIDO("Concluído");

    private final String rotulo;

    Status(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    public String toString() {
        return rotulo;
    }
}
