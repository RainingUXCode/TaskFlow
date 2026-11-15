package com.taskflow.view;

import com.taskflow.model.Status;
import com.taskflow.service.GerenciadorTarefas;
import java.awt.Component;
import java.awt.Container;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public final class NotificacaoObservadorTeste {
    private NotificacaoObservadorTeste() {
    }

    public static void main(String[] args) throws Exception {
        var expirou = new CountDownLatch(1);
        SwingUtilities.invokeAndWait(() -> testarFluxo(expirou));
        verificar(expirou.await(5, TimeUnit.SECONDS), "A notificação deveria desaparecer sozinha");
        System.out.println("OK - Criação, movimentação, exclusão e expiração com os três observadores");
    }

    private static void testarFluxo(CountDownLatch expirou) {
        var gerenciador = new GerenciadorTarefas();
        var contadores = new ContadorObservador();
        var quadro = new QuadroObservador(gerenciador::alterarStatus, gerenciador::excluirTarefa,
                contadores::componente);
        var notificacao = new NotificacaoObservador(150);
        gerenciador.registrarObservador(quadro);
        gerenciador.registrarObservador(contadores);
        gerenciador.registrarObservador(notificacao);
        var aviso = notificacao.componente();

        verificar(!aviso.isVisible(), "O aviso começa oculto");
        verificarEstado(quadro, contadores, null);
        var tarefa = gerenciador.criarTarefa("Revisar quadro", "");
        verificarAviso(aviso, "Tarefa criada · Revisar quadro");
        verificarEstado(quadro, contadores, Status.A_FAZER);

        verificar(buscarBotao(quadro.painel(), "Mover para A Fazer: Revisar quadro") == null,
                "A primeira etapa não deve ter seta para trás");
        clicar(quadro, "Mover para Em Andamento: Revisar quadro");
        verificarAviso(aviso, "Movida para Em Andamento · Revisar quadro");
        verificarEstado(quadro, contadores, Status.EM_ANDAMENTO);
        verificar(!gerenciador.alterarStatus(tarefa.id(), Status.EM_ANDAMENTO), "Movimento repetido");
        verificarAviso(aviso, "Movida para Em Andamento · Revisar quadro");

        clicar(quadro, "Mover para Concluído: Revisar quadro");
        verificarEstado(quadro, contadores, Status.CONCLUIDO);
        verificar(buscarBotao(quadro.painel(), "Mover para Concluído: Revisar quadro") == null,
                "A última etapa não deve ter seta para frente");
        clicar(quadro, "Mover para Em Andamento: Revisar quadro");
        clicar(quadro, "Mover para A Fazer: Revisar quadro");
        verificarAviso(aviso, "Movida para A Fazer · Revisar quadro");
        verificarEstado(quadro, contadores, Status.A_FAZER);
        clicar(quadro, "Excluir Revisar quadro");
        verificarAviso(aviso, "Tarefa excluída · Revisar quadro");
        verificarEstado(quadro, contadores, null);

        quadro.buscar("REVISAO");
        var revisao = gerenciador.criarTarefa("Revisão", "", Status.CONCLUIDO);
        verificarAviso(aviso, "Tarefa criada · Revisão");
        var concluido = quadro.painel().getComponent(Status.CONCLUIDO.ordinal());
        verificar(buscarBotao(concluido, "Excluir Revisão") != null, "Busca deve aceitar variações de acento e caixa");
        var outra = gerenciador.criarTarefa("Planejar", "revisão", Status.CONCLUIDO);
        verificar(buscarBotao(concluido, "Excluir Planejar") == null, "Descrição não deve entrar na busca");
        verificar(contem(contadores.componente(Status.CONCLUIDO), JLabel.class, "2"), "Busca mudou o total");
        quadro.buscar("");
        verificar(buscarBotao(concluido, "Excluir Planejar") != null, "Limpar a busca deve restaurar o quadro");
        gerenciador.excluirTarefa(outra.id());
        gerenciador.excluirTarefa(revisao.id());
        verificarEstado(quadro, contadores, null);
        aviso.addPropertyChangeListener("text", evento -> {
            if ("".equals(evento.getNewValue()) && !aviso.isVisible() && SwingUtilities.isEventDispatchThread()) {
                expirou.countDown();
            }
        });
    }

    private static void verificarEstado(QuadroObservador quadro, ContadorObservador contadores, Status ocupado) {
        for (Status status : Status.values()) {
            var coluna = quadro.painel().getComponent(status.ordinal());
            boolean temCartao = buscarBotao(coluna, "Excluir Revisar quadro") != null;
            boolean temEstadoVazio = contem(coluna, JLabel.class, "Etapa vazia");
            boolean temTarefa = ocupado == status;
            verificar(temCartao == temTarefa && temEstadoVazio != temTarefa, "Quadro incorreto em " + status);
            var contador = contadores.componente(status);
            verificar(contem(contador, JLabel.class, temTarefa ? "1" : "0"), "Total incorreto em " + status);
            verificar(contem(coluna, JLabel.class, temTarefa ? "1" : "0"), "Total incorreto na coluna " + status);
        }
    }

    private static void clicar(QuadroObservador quadro, String nome) {
        var botao = buscarBotao(quadro.painel(), nome);
        verificar(botao != null && botao.isEnabled() && botao.isFocusable(), "Botão inacessível: " + nome);
        verificar(nome.equals(botao.getToolTipText()), "Dica ausente: " + nome);
        botao.doClick(0);
    }

    private static JButton buscarBotao(Component componente, String nome) {
        if (componente instanceof JButton botao && nome.equals(botao.getAccessibleContext().getAccessibleName())) {
            return botao;
        }
        if (componente instanceof Container container) {
            for (Component filho : container.getComponents()) {
                var botao = buscarBotao(filho, nome);
                if (botao != null) {
                    return botao;
                }
            }
        }
        return null;
    }

    private static boolean contem(Component componente, Class<?> tipo, String texto) {
        if (tipo.isInstance(componente) && (texto == null || texto.equals(((JLabel) componente).getText()))) {
            return true;
        }
        return componente instanceof Container container && Arrays.stream(container.getComponents())
                .anyMatch(filho -> contem(filho, tipo, texto));
    }

    private static void verificarAviso(JLabel aviso, String esperado) {
        verificar(aviso.isVisible() && esperado.equals(aviso.getText()), "Aviso incorreto: " + aviso.getText());
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }
}
