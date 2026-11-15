package com.taskflow.view;

import com.taskflow.model.Status;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public final class TaskFlowFrame extends JFrame {
    @FunctionalInterface
    public interface CriarTarefa {
        void criar(String titulo, String descricao, Status status);
    }

    public TaskFlowFrame(QuadroObservador quadro, NotificacaoObservador notificacao, CriarTarefa criarTarefa) {
        super("TaskFlow");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 440));
        setSize(1220, 720);
        setLocationRelativeTo(null);

        var conteudo = new JPanel(new BorderLayout());
        conteudo.setBackground(Tema.FUNDO_AZUL);
        conteudo.add(cabecalho(quadro, criarTarefa), BorderLayout.NORTH);
        setContentPane(conteudo);

        var corpo = new JPanel(new BorderLayout(0, 16));
        corpo.setOpaque(false);
        corpo.setBorder(BorderFactory.createEmptyBorder(24, 32, 16, 32));
        corpo.add(quadro.painel(), BorderLayout.CENTER);

        var rodape = new JPanel(new BorderLayout());
        rodape.setOpaque(false);
        rodape.setPreferredSize(new Dimension(0, 64));
        rodape.add(notificacao.componente(), BorderLayout.EAST);
        corpo.add(rodape, BorderLayout.SOUTH);
        conteudo.add(corpo, BorderLayout.CENTER);
    }

    private JPanel cabecalho(QuadroObservador quadro, CriarTarefa criarTarefa) {
        var faixa = Tema.painelGradiente(new BorderLayout(24, 0), Tema.MARINHO, Tema.MARINHO_ESCURO, true);
        faixa.setBorder(BorderFactory.createEmptyBorder(16, 32, 16, 32));

        var marca = new JPanel(new BorderLayout(16, 0));
        marca.setOpaque(false);
        var icone = Tema.painelArredondado(new GridBagLayout(), Tema.DESTAQUE, 19);
        icone.setPreferredSize(new Dimension(48, 48));
        icone.add(new JLabel(new Tema.Simbolo(Tema.Glifo.QUADRO, Tema.SUPERFICIE, 26)));
        marca.add(icone, BorderLayout.WEST);

        var nomeQuadro = new JPanel();
        nomeQuadro.setLayout(new BoxLayout(nomeQuadro, BoxLayout.Y_AXIS));
        nomeQuadro.setOpaque(false);
        nomeQuadro.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        var nome = new JLabel("TaskFlow");
        nome.setFont(Tema.TEXTO.deriveFont(Font.BOLD, 27f));
        nome.setForeground(Tema.SUPERFICIE);
        var subtitulo = new JLabel("Meu quadro");
        subtitulo.setFont(Tema.TEXTO.deriveFont(14f));
        subtitulo.setForeground(Tema.TEXTO_CLARO);
        subtitulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        nomeQuadro.add(nome);
        nomeQuadro.add(subtitulo);
        marca.add(nomeQuadro, BorderLayout.CENTER);
        faixa.add(marca, BorderLayout.WEST);

        var nova = Tema.botao("Nova tarefa", Tema.DESTAQUE, Tema.SUPERFICIE);
        nova.setFont(Tema.TEXTO.deriveFont(Font.BOLD, 14f));
        nova.setIcon(new Tema.Simbolo(Tema.Glifo.MAIS, Tema.SUPERFICIE, 20));
        nova.setIconTextGap(8);
        nova.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        nova.addActionListener(e -> abrirCadastro(criarTarefa));
        faixa.add(nova, BorderLayout.EAST);
        var centro = new JPanel(new GridBagLayout());
        centro.setOpaque(false);
        centro.add(campoBusca(quadro));
        faixa.add(centro, BorderLayout.CENTER);
        return faixa;
    }

    private JPanel campoBusca(QuadroObservador quadro) {
        var painel = Tema.painelArredondado(new BorderLayout(8, 0), Tema.FUNDO, 14);
        painel.setBorder(Tema.bordaArredondada(Tema.BORDA, 14, 8, 16));
        painel.add(new JLabel(new Tema.Simbolo(Tema.Glifo.BUSCA, Tema.SECUNDARIA, 18)), BorderLayout.WEST);
        var busca = new JTextField(22) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getDocument().getLength() == 0) {
                    Tema.desenharDica(g, this, "Buscar tarefas...", true);
                }
            }
        };
        busca.setOpaque(false);
        busca.setBorder(BorderFactory.createEmptyBorder());
        busca.setFont(Tema.TEXTO);
        busca.setForeground(Tema.TEXTO_PRINCIPAL);
        busca.getAccessibleContext().setAccessibleName("Buscar tarefas por título");
        busca.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent evento) {
                painel.setBorder(Tema.bordaArredondada(Tema.DESTAQUE, 14, 8, 16));
            }

            @Override
            public void focusLost(FocusEvent evento) {
                painel.setBorder(Tema.bordaArredondada(Tema.BORDA, 14, 8, 16));
            }
        });
        busca.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { quadro.buscar(busca.getText()); }
            @Override
            public void removeUpdate(DocumentEvent e) { quadro.buscar(busca.getText()); }
            @Override
            public void changedUpdate(DocumentEvent e) { quadro.buscar(busca.getText()); }
        });
        painel.add(busca, BorderLayout.CENTER);
        return painel;
    }

    private void abrirCadastro(CriarTarefa criarTarefa) {
        var dialogo = new CadastroTarefaDialog(this, criarTarefa);
        var anterior = getGlassPane();
        boolean estavaVisivel = anterior.isVisible();
        setGlassPane(overlay(getContentPane()));
        getGlassPane().setVisible(true);
        try {
            dialogo.setVisible(true);
        } finally {
            setGlassPane(anterior);
            anterior.setVisible(estavaVisivel);
        }
    }

    private static JPanel overlay(Container conteudo) {
        int largura = Math.max(1, conteudo.getWidth());
        int altura = Math.max(1, conteudo.getHeight());
        var reduzida = new BufferedImage(Math.max(3, largura / 3), Math.max(3, altura / 3),
                BufferedImage.TYPE_INT_RGB);
        var captura = reduzida.createGraphics();
        captura.scale((double) reduzida.getWidth() / largura, (double) reduzida.getHeight() / altura);
        conteudo.printAll(captura);
        captura.dispose();
        var suavizacao = new Kernel(3, 3, new float[]{
            .0625f, .125f, .0625f,
            .125f, .25f, .125f,
            .0625f, .125f, .0625f
        });
        var fundoDesfocado = new ConvolveOp(suavizacao, ConvolveOp.EDGE_NO_OP, null).filter(reduzida, null);
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                var desenho = (Graphics2D) g.create();
                desenho.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                desenho.drawImage(fundoDesfocado, 0, 0, getWidth(), getHeight(), null);
                desenho.setColor(new Color(15, 23, 42, 128));
                desenho.fillRect(0, 0, getWidth(), getHeight());
                desenho.dispose();
            }
        };
    }
}
