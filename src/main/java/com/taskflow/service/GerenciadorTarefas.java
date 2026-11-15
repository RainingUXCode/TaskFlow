package com.taskflow.service;

import com.taskflow.model.Status;
import com.taskflow.model.Tarefa;
import com.taskflow.observer.EventoTarefa;
import com.taskflow.observer.EventoTarefa.Tipo;
import com.taskflow.observer.Observador;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Use em uma única thread, inclusive para inscrição e remoção de observadores. */
public final class GerenciadorTarefas {
    private static final Logger LOGGER = Logger.getLogger(GerenciadorTarefas.class.getName());

    private final Map<UUID, Tarefa> tarefas = new LinkedHashMap<>();
    private final Set<Observador> observadores = new LinkedHashSet<>();
    private boolean notificando;

    public void registrarObservador(Observador observador) {
        observadores.add(Objects.requireNonNull(observador));
    }

    public void removerObservador(Observador observador) {
        observadores.remove(observador);
    }

    public List<Tarefa> listarTarefas() {
        return List.copyOf(tarefas.values());
    }

    public Tarefa criarTarefa(String titulo, String descricao) {
        return criarTarefa(titulo, descricao, Status.A_FAZER);
    }

    public Tarefa criarTarefa(String titulo, String descricao, Status statusInicial) {
        verificarAlteracaoPermitida();
        UUID id;
        do {
            id = UUID.randomUUID();
        } while (tarefas.containsKey(id));

        Tarefa tarefa = new Tarefa(id, titulo, descricao, statusInicial);
        tarefas.put(id, tarefa);
        notificar(Tipo.CRIADA, tarefa);
        return tarefa;
    }

    public boolean alterarStatus(UUID id, Status novoStatus) {
        verificarAlteracaoPermitida();
        Tarefa tarefa = tarefas.get(id);
        if (tarefa == null || novoStatus == null || tarefa.status() == novoStatus) {
            return false;
        }
        Tarefa atualizada = tarefa.comStatus(novoStatus);
        tarefas.put(id, atualizada);
        notificar(Tipo.STATUS_ALTERADO, atualizada);
        return true;
    }

    public boolean excluirTarefa(UUID id) {
        verificarAlteracaoPermitida();
        Tarefa removida = tarefas.remove(id);
        if (removida == null) {
            return false;
        }
        notificar(Tipo.EXCLUIDA, removida);
        return true;
    }

    private void verificarAlteracaoPermitida() {
        if (notificando) {
            throw new IllegalStateException("O observador deve apenas reagir ao evento.");
        }
    }

    private void notificar(Tipo tipo, Tarefa tarefa) {
        EventoTarefa evento = new EventoTarefa(tipo, tarefa, listarTarefas());
        notificando = true;
        try {
            for (Observador observador : List.copyOf(observadores)) {
                try {
                    observador.atualizar(evento);
                } catch (RuntimeException erro) {
                    LOGGER.log(Level.WARNING, "Falha ao processar o evento " + tipo, erro);
                }
            }
        } finally {
            notificando = false;
        }
    }
}
