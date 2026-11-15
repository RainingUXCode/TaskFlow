package com.taskflow;

import com.taskflow.service.GerenciadorTarefas;
import com.taskflow.view.ContadorObservador;
import com.taskflow.view.NotificacaoObservador;
import com.taskflow.view.QuadroObservador;
import com.taskflow.view.TaskFlowFrame;
import javax.swing.SwingUtilities;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            var gerenciador = new GerenciadorTarefas();
            var contadores = new ContadorObservador();
            var quadro = new QuadroObservador(gerenciador::alterarStatus, gerenciador::excluirTarefa,
                    contadores::componente);
            var notificacao = new NotificacaoObservador();
            gerenciador.registrarObservador(quadro);
            gerenciador.registrarObservador(contadores);
            gerenciador.registrarObservador(notificacao);

            var janela = new TaskFlowFrame(quadro, notificacao, gerenciador::criarTarefa);
            janela.setVisible(true);
        });
    }
}
