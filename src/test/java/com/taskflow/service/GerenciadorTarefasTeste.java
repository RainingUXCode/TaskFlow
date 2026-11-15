package com.taskflow.service;

import com.taskflow.model.Status;
import com.taskflow.observer.EventoTarefa;
import com.taskflow.observer.EventoTarefa.Tipo;
import com.taskflow.observer.Observador;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class GerenciadorTarefasTeste {
    private GerenciadorTarefasTeste() {
    }

    public static void main(String[] args) {
        testar("Criação e IDs únicos", GerenciadorTarefasTeste::criacao);
        testar("Alteração de status", GerenciadorTarefasTeste::alteracao);
        testar("Exclusão", GerenciadorTarefasTeste::exclusao);
        testar("Registro e remoção de observadores", GerenciadorTarefasTeste::inscricao);
        testar("Notificação após a mudança e snapshots imutáveis", GerenciadorTarefasTeste::eventos);
        testar("Operações inválidas e repetidas não notificam", GerenciadorTarefasTeste::invalidas);
        testar("Status inicial escolhido e compatibilidade", GerenciadorTarefasTeste::statusInicial);
        testar("Falha de observador não interrompe os demais", GerenciadorTarefasTeste::falhaIsolada);
        testar("Alteração durante notificação é bloqueada", GerenciadorTarefasTeste::alteracaoDuranteNotificacao);
    }

    private static void criacao() {
        var gerenciador = new GerenciadorTarefas();
        var primeira = gerenciador.criarTarefa("  Estudar Observer  ", "  Revisar contratos  ");
        var segunda = gerenciador.criarTarefa("Estudar Observer", "");
        verificar(!primeira.id().equals(segunda.id()), "Cada tarefa deve ter um ID único");
        verificar(primeira.titulo().equals("Estudar Observer"), "Título deve ser normalizado");
        verificar(primeira.descricao().equals("Revisar contratos"), "Descrição deve ser armazenada");
        verificar(primeira.status() == Status.A_FAZER, "Status inicial deve ser A Fazer");
        verificar(gerenciador.listarTarefas().equals(List.of(primeira, segunda)), "Tarefas ausentes");
    }

    private static void alteracao() {
        var gerenciador = new GerenciadorTarefas();
        var tarefa = gerenciador.criarTarefa("Estudar", "");
        for (Status status : List.of(Status.EM_ANDAMENTO, Status.CONCLUIDO, Status.A_FAZER)) {
            verificar(gerenciador.alterarStatus(tarefa.id(), status), "Alteração deve ter sucesso");
            verificar(gerenciador.listarTarefas().equals(List.of(tarefa.comStatus(status))),
                    "Alteração deve preservar os demais dados");
        }
    }

    private static void exclusao() {
        var gerenciador = new GerenciadorTarefas();
        var tarefa = gerenciador.criarTarefa("Estudar", "");
        var outra = gerenciador.criarTarefa("Revisar", "");
        verificar(gerenciador.excluirTarefa(tarefa.id()), "Exclusão deve ter sucesso");
        verificar(gerenciador.listarTarefas().equals(List.of(outra)), "Somente a tarefa indicada deve sair");
    }

    private static void inscricao() {
        var gerenciador = new GerenciadorTarefas();
        var recebidos = new ArrayList<EventoTarefa>();
        var outrosEventos = new ArrayList<EventoTarefa>();
        Observador observador = recebidos::add;
        gerenciador.registrarObservador(observador);
        gerenciador.registrarObservador(observador);
        gerenciador.registrarObservador(outrosEventos::add);
        gerenciador.criarTarefa("Primeira", "");
        gerenciador.removerObservador(observador);
        gerenciador.criarTarefa("Segunda", "");
        verificar(recebidos.size() == 1, "Inscrição duplicada ou remoção ineficaz");
        verificar(outrosEventos.size() == 2, "O segundo observador deve continuar inscrito");
    }

    private static void eventos() {
        var gerenciador = new GerenciadorTarefas();
        var recebidos = new ArrayList<EventoTarefa>();
        gerenciador.registrarObservador(evento -> {
            verificar(gerenciador.listarTarefas().equals(evento.tarefas()), "Evento antes da alteração");
            recebidos.add(evento);
        });
        var criada = gerenciador.criarTarefa("Estudar", "");
        var atualizada = criada.comStatus(Status.CONCLUIDO);
        gerenciador.alterarStatus(criada.id(), Status.CONCLUIDO);
        gerenciador.excluirTarefa(criada.id());
        verificar(recebidos.equals(List.of(
                new EventoTarefa(Tipo.CRIADA, criada, List.of(criada)),
                new EventoTarefa(Tipo.STATUS_ALTERADO, atualizada, List.of(atualizada)),
                new EventoTarefa(Tipo.EXCLUIDA, atualizada, List.of()))), "Eventos fora do contrato");
        esperar(UnsupportedOperationException.class, () -> recebidos.get(0).tarefas().clear());
        esperar(UnsupportedOperationException.class, () -> gerenciador.listarTarefas().add(criada));
    }

    private static void invalidas() {
        var gerenciador = new GerenciadorTarefas();
        var existente = gerenciador.criarTarefa("Estudar", "");
        var recebidos = new ArrayList<EventoTarefa>();
        gerenciador.registrarObservador(recebidos::add);
        esperar(IllegalArgumentException.class, () -> gerenciador.criarTarefa(" \t ", ""));
        esperar(IllegalArgumentException.class, () -> gerenciador.criarTarefa(null, ""));
        var ausente = UUID.randomUUID();
        verificar(!gerenciador.alterarStatus(ausente, Status.CONCLUIDO), "ID ausente deve ser ignorado");
        verificar(!gerenciador.alterarStatus(existente.id(), null), "Status nulo deve ser ignorado");
        verificar(!gerenciador.alterarStatus(existente.id(), existente.status()), "Status igual não altera");
        verificar(!gerenciador.excluirTarefa(ausente), "Exclusão de ID ausente deve ser ignorada");
        verificar(recebidos.isEmpty(), "Operações inválidas não devem emitir eventos");
        verificar(gerenciador.listarTarefas().equals(List.of(existente)), "Estado mudou indevidamente");
    }


    private static void statusInicial() {
        var gerenciador = new GerenciadorTarefas();
        var eventos = new ArrayList<EventoTarefa>();
        gerenciador.registrarObservador(eventos::add);
        var aFazer = gerenciador.criarTarefa("A", "");
        var emAndamento = gerenciador.criarTarefa("B", "", Status.EM_ANDAMENTO);
        var concluida = gerenciador.criarTarefa("C", "", Status.CONCLUIDO);
        verificar(aFazer.status() == Status.A_FAZER, "O cadastro antigo deve manter o padrão A Fazer");
        verificar(emAndamento.status() == Status.EM_ANDAMENTO, "Status inicial Em Andamento ignorado");
        verificar(concluida.status() == Status.CONCLUIDO, "Status inicial Concluído ignorado");
        verificar(eventos.size() == 3, "Cada cadastro válido deve emitir um evento");
        verificar(eventos.get(2).tarefas().equals(List.of(aFazer, emAndamento, concluida)),
                "O último evento deve incluir as tarefas de todos os status");
        esperar(NullPointerException.class, () -> gerenciador.criarTarefa("D", "", null));
        verificar(eventos.size() == 3 && gerenciador.listarTarefas().size() == 3,
                "Status inicial inválido não deve alterar o estado nem publicar evento");
    }

    private static void falhaIsolada() {
        var gerenciador = new GerenciadorTarefas();
        var recebidos = new ArrayList<EventoTarefa>();
        gerenciador.registrarObservador(evento -> { throw new IllegalStateException("Falha proposital do teste"); });
        gerenciador.registrarObservador(recebidos::add);
        var tarefa = gerenciador.criarTarefa("Revisar", "");
        verificar(recebidos.size() == 1, "Falha do primeiro observador bloqueou o segundo");
        verificar(recebidos.get(0).tarefa().equals(tarefa), "Segundo observador recebeu evento errado");
        verificar(gerenciador.listarTarefas().equals(List.of(tarefa)), "Falha de observador perdeu tarefa");
    }

    private static void alteracaoDuranteNotificacao() {
        var gerenciador = new GerenciadorTarefas();
        var bloqueios = new ArrayList<Boolean>();
        var recebidos = new ArrayList<EventoTarefa>();
        gerenciador.registrarObservador(evento -> {
            try {
                gerenciador.criarTarefa("Indevida", "");
                bloqueios.add(false);
            } catch (IllegalStateException esperado) {
                bloqueios.add(true);
            }
        });
        gerenciador.registrarObservador(recebidos::add);
        var tarefa = gerenciador.criarTarefa("Válida", "");
        verificar(bloqueios.equals(List.of(true)), "O observador conseguiu alterar tarefas durante a notificação");
        verificar(recebidos.size() == 1, "Notificação deveria continuar após a tentativa bloqueada");
        verificar(gerenciador.listarTarefas().equals(List.of(tarefa)), "Uma tarefa indevida foi adicionada");
        var nova = gerenciador.criarTarefa("Após notificação", "");
        verificar(gerenciador.listarTarefas().contains(nova), "Gerenciador permaneceu bloqueado após notificar");
    }

    private static void testar(String nome, Runnable teste) {
        teste.run();
        System.out.println("OK - " + nome);
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }

    private static void esperar(Class<? extends RuntimeException> tipo, Runnable operacao) {
        try {
            operacao.run();
        } catch (RuntimeException erro) {
            if (tipo.isInstance(erro)) {
                return;
            }
            throw erro;
        }
        throw new AssertionError("Exceção esperada: " + tipo.getSimpleName());
    }
}
