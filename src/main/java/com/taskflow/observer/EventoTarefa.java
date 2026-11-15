package com.taskflow.observer;

import com.taskflow.model.Tarefa;
import java.util.List;

public record EventoTarefa(Tipo tipo, Tarefa tarefa, List<Tarefa> tarefas) {
    public EventoTarefa {
        tarefas = List.copyOf(tarefas);
    }

    public enum Tipo {
        CRIADA, STATUS_ALTERADO, EXCLUIDA
    }
}
