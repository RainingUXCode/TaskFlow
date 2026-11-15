package com.taskflow.view;

import com.taskflow.observer.EventoTarefa;
import com.taskflow.observer.Observador;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public final class NotificacaoObservador implements Observador {
    private Color faixa = Tema.SUCESSO;
    private final JLabel icone = new JLabel();
    private final JLabel tituloAviso = new JLabel();
    private final JLabel detalhe = new JLabel();
    private final JLabel aviso = new JLabel() {
        @Override
        protected void paintComponent(Graphics g) {
            int altura = getHeight() - 5;
            Tema.sombra(g, getWidth(), altura, Tema.RAIO);
            Tema.gradiente(g, Tema.MARINHO_ESCURO, Tema.CHIP, getWidth(), altura, Tema.RAIO, true);
            var d = (Graphics2D) g.create();
            d.clip(new RoundRectangle2D.Double(0, 0, getWidth(), altura, Tema.RAIO, Tema.RAIO));
            d.setColor(faixa);
            d.fillRect(0, 0, 5, altura);
            d.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension tamanho = getLayout().preferredLayoutSize(this);
            return new Dimension(Math.min(Math.max(tamanho.width + 4, 320), 470), tamanho.height);
        }
    };
    private final Timer temporizador;

    public NotificacaoObservador() {
        this(3_500);
    }

    NotificacaoObservador(int duracaoEmMillis) {
        tituloAviso.setFont(Tema.TEXTO.deriveFont(Font.BOLD, 15f));
        tituloAviso.setForeground(Tema.SUPERFICIE);
        detalhe.setFont(Tema.TEXTO.deriveFont(13f));
        detalhe.setForeground(Tema.TEXTO_CLARO);
        detalhe.putClientProperty("html.disable", Boolean.TRUE);
        var textos = new JPanel(new BorderLayout(0, 2));
        textos.setOpaque(false);
        textos.add(tituloAviso, BorderLayout.NORTH);
        textos.add(detalhe, BorderLayout.CENTER);
        aviso.setLayout(new BorderLayout(12, 0));
        aviso.setBorder(BorderFactory.createEmptyBorder(10, 16, 15, 16));
        aviso.add(icone, BorderLayout.WEST);
        aviso.add(textos, BorderLayout.CENTER);
        aviso.setVisible(false);
        temporizador = new Timer(duracaoEmMillis, e -> {
            aviso.setVisible(false);
            aviso.setText("");
        });
        temporizador.setRepeats(false);
    }

    JLabel componente() {
        return aviso;
    }

    @Override
    public void atualizar(EventoTarefa evento) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> atualizar(evento));
            return;
        }

        var tarefa = evento.tarefa();
        String titulo = tarefa.titulo().replaceAll("\\R", " ");
        String chamada = switch (evento.tipo()) {
            case CRIADA -> "Tarefa criada";
            case STATUS_ALTERADO -> "Movida para " + tarefa.status();
            case EXCLUIDA -> "Tarefa excluída";
        };
        faixa = switch (evento.tipo()) {
            case CRIADA -> Tema.SUCESSO;
            case STATUS_ALTERADO -> Tema.corStatus(tarefa.status());
            case EXCLUIDA -> Tema.EXCLUIR;
        };
        var glifo = switch (evento.tipo()) {
            case CRIADA -> Tema.Glifo.MARCA;
            case STATUS_ALTERADO -> Tema.Glifo.DIREITA;
            case EXCLUIDA -> Tema.Glifo.LIXEIRA;
        };
        icone.setIcon(Tema.selo(glifo, faixa, 31));
        tituloAviso.setText(chamada);
        detalhe.setText(switch (evento.tipo()) {
            case CRIADA -> "“" + titulo + "” foi adicionada ao quadro.";
            case STATUS_ALTERADO -> "A tarefa “" + titulo + "” mudou de etapa.";
            case EXCLUIDA -> "“" + titulo + "” foi removida do quadro.";
        });
        aviso.setText(chamada + " · " + titulo);
        aviso.setVisible(true);
        temporizador.restart();
    }
}
