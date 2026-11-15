package com.taskflow.model;

import java.util.Objects;
import java.util.UUID;

public record Tarefa(UUID id, String titulo, String descricao, Status status) {
    public Tarefa {
        Objects.requireNonNull(id);
        Objects.requireNonNull(status);
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("Informe o título da tarefa.");
        }
        titulo = titulo.strip();
        descricao = descricao == null ? "" : descricao.strip();
    }

    public Tarefa comStatus(Status novoStatus) {
        return new Tarefa(id, titulo, descricao, novoStatus);
    }
}
