package com.taskflow.view;

import com.taskflow.model.Status;
import com.taskflow.model.Tarefa;
import com.taskflow.observer.EventoTarefa;
import com.taskflow.observer.Observador;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.Normalizer;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

public final class QuadroObservador implements Observador {
    private final JPanel painel = new JPanel(new GridLayout(1, 0, 16, 0));
    private final Map<Status, JPanel> listas = new EnumMap<>(Status.class);
    private final BiConsumer<UUID, Status> alterarStatus;
    private final Consumer<UUID> excluirTarefa;
    private List<Tarefa> tarefas = List.of();
    private String busca = "";

    public QuadroObservador(BiConsumer<UUID, Status> alterarStatus, Consumer<UUID> excluirTarefa,
            Function<Status, JComponent> contador) {
        this.alterarStatus = alterarStatus;
        this.excluirTarefa = excluirTarefa;
        painel.setOpaque(false);
        for (Status status : Status.values()) {
            var cor = Tema.corStatus(status);
            var coluna = Tema.painelArredondado(new BorderLayout(0, 16), Tema.FUNDO, Tema.RAIO);
            coluna.setBorder(Tema.bordaCartao(16));
            var glifo = switch (status) {
                case A_FAZER -> Tema.Glifo.LISTA;
                case EM_ANDAMENTO -> Tema.Glifo.RAIO;
                case CONCLUIDO -> Tema.Glifo.MARCA;
            };
            var titulo = new JLabel(status.toString(), Tema.selo(glifo, cor, 28), SwingConstants.LEADING);
            titulo.setIconTextGap(Tema.ESPACO);
            titulo.setFont(Tema.TEXTO.deriveFont(Font.BOLD, 18f));
            titulo.setForeground(Tema.TEXTO_PRINCIPAL);
            var cabecalho = new JPanel(new BorderLayout(Tema.ESPACO, 0));
            cabecalho.setOpaque(false);
            cabecalho.add(titulo, BorderLayout.CENTER);
            cabecalho.add(contador.apply(status), BorderLayout.EAST);
            coluna.add(cabecalho, BorderLayout.NORTH);

            var lista = new ListaCartoes();
            lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
            lista.setOpaque(false);
            var rolagem = new JScrollPane(lista, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            rolagem.setBorder(BorderFactory.createEmptyBorder());
            rolagem.setOpaque(false);
            rolagem.getViewport().setOpaque(false);
            rolagem.getViewport().addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent evento) {
                    for (Component cartao : lista.getComponents()) {
                        cartao.invalidate();
                    }
                    lista.revalidate();
                }
            });
            coluna.add(rolagem, BorderLayout.CENTER);
            listas.put(status, lista);
            painel.add(coluna);
        }
        renderizar();
    }

    JPanel painel() {
        return painel;
    }

    @Override
    public void atualizar(EventoTarefa evento) {
        tarefas = evento.tarefas();
        renderizar();
    }

    void buscar(String texto) {
        busca = normalizar(texto.strip());
        renderizar();
    }

    private static String normalizar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
    }

    private void renderizar() {
        listas.values().forEach(JPanel::removeAll);
        for (Tarefa tarefa : tarefas) {
            if (busca.isEmpty() || normalizar(tarefa.titulo()).contains(busca)) {
                listas.get(tarefa.status()).add(criarCartao(tarefa));
            }
        }
        for (JPanel lista : listas.values()) {
            if (lista.getComponentCount() == 0) {
                adicionarEstadoVazio(lista);
            }
        }
        painel.revalidate();
        painel.repaint();
    }

    private void adicionarEstadoVazio(JPanel lista) {
        var bloco = new JPanel() {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        bloco.setOpaque(false);
        bloco.setLayout(new BoxLayout(bloco, BoxLayout.Y_AXIS));
        bloco.setBorder(BorderFactory.createEmptyBorder(32, 0, 24, 0));
        var icone = new JLabel(new Tema.Simbolo(busca.isEmpty() ? Tema.Glifo.LISTA : Tema.Glifo.BUSCA,
                Tema.tonalizar(Tema.FUNDO, Tema.SECUNDARIA, .38f), 28));
        icone.setAlignmentX(Component.CENTER_ALIGNMENT);
        bloco.add(icone);
        bloco.add(Box.createVerticalStrut(16));
        var titulo = new JLabel(busca.isEmpty() ? "Etapa vazia" : "Nenhum título encontrado");
        titulo.setFont(Tema.TEXTO.deriveFont(Font.BOLD, 14f));
        titulo.setForeground(Tema.TEXTO_PRINCIPAL);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        bloco.add(titulo);
        bloco.add(Box.createVerticalStrut(8));
        var dica = new JLabel(busca.isEmpty() ? "Use Nova tarefa para começar." : "Tente outro termo de busca.");
        dica.setFont(Tema.TEXTO.deriveFont(13f));
        dica.setForeground(Tema.SECUNDARIA);
        dica.setAlignmentX(Component.CENTER_ALIGNMENT);
        bloco.add(dica);
        lista.add(bloco);
    }

    private JPanel criarCartao(Tarefa tarefa) {
        var titulo = criarTexto(tarefa.titulo(), Tema.TEXTO.deriveFont(Font.BOLD, 16f), Tema.TEXTO_PRINCIPAL);
        var descricao = criarTexto(tarefa.descricao(), Tema.TEXTO, Tema.SECUNDARIA);
        var cartao = new JPanel(new BorderLayout(0, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                int altura = getHeight() - Tema.ESPACO;
                Tema.sombra(g, getWidth(), altura, Tema.RAIO);
                Tema.preencher(g, Tema.SUPERFICIE, getWidth(), altura, Tema.RAIO);
                super.paintComponent(g);
            }

            @Override
            public Dimension getPreferredSize() {
                // O BoxLayout precisa da altura já calculada para a largura do viewport.
                var viewport = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, this);
                if (viewport != null && viewport.getWidth() > 0) {
                    var margens = getInsets();
                    int largura = Math.max(1, viewport.getWidth() - margens.left - margens.right - 11);
                    titulo.setSize(largura, Short.MAX_VALUE);
                    descricao.setSize(largura, Short.MAX_VALUE);
                }
                return super.getPreferredSize();
            }

            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        cartao.setOpaque(false);
        cartao.setAlignmentX(Component.CENTER_ALIGNMENT);
        cartao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 0, Tema.ESPACO, 0), Tema.bordaCartao(12)));
        var texto = new JPanel(new BorderLayout(0, 3));
        texto.setOpaque(false);
        texto.add(titulo, BorderLayout.NORTH);
        if (!tarefa.descricao().isEmpty()) {
            texto.add(descricao, BorderLayout.CENTER);
        }
        var faixa = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Tema.preencher(g, Tema.corStatus(tarefa.status()), getWidth(), getHeight(), getWidth());
            }
        };
        faixa.setOpaque(false);
        faixa.setPreferredSize(new Dimension(3, 0));
        var conteudo = new JPanel(new BorderLayout(8, 0));
        conteudo.setOpaque(false);
        conteudo.add(faixa, BorderLayout.WEST);
        conteudo.add(texto, BorderLayout.CENTER);
        cartao.add(conteudo, BorderLayout.CENTER);

        var controles = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        controles.setOpaque(false);
        var etapas = Status.values();
        int posicao = tarefa.status().ordinal();
        if (posicao > 0) {
            controles.add(botaoMover(tarefa, etapas[posicao - 1], Tema.Glifo.ESQUERDA));
        }
        if (posicao < etapas.length - 1) {
            controles.add(botaoMover(tarefa, etapas[posicao + 1], Tema.Glifo.DIREITA));
        }
        var excluir = botaoIcone("Excluir " + tarefa.titulo(), Tema.Glifo.LIXEIRA, Tema.EXCLUIR);
        excluir.addActionListener(e -> excluirTarefa.accept(tarefa.id()));
        controles.add(excluir);
        cartao.add(controles, BorderLayout.SOUTH);
        return cartao;
    }

    private JButton botaoMover(Tarefa tarefa, Status destino, Tema.Glifo seta) {
        var botao = botaoIcone("Mover para " + destino + ": " + tarefa.titulo(), seta, Tema.SECUNDARIA);
        botao.addActionListener(e -> alterarStatus.accept(tarefa.id(), destino));
        return botao;
    }

    private JButton botaoIcone(String nome, Tema.Glifo glifo, Color cor) {
        var botao = Tema.botao("", Tema.SUPERFICIE, cor);
        botao.setIcon(new Tema.Simbolo(glifo, cor, 17));
        botao.setBorder(BorderFactory.createEmptyBorder(6, 9, 6, 9));
        botao.setToolTipText(nome);
        botao.getAccessibleContext().setAccessibleName(nome);
        return botao;
    }

    private JTextArea criarTexto(String texto, Font fonte, Color cor) {
        var area = new JTextArea(texto, 0, 16);
        area.setEditable(false);
        area.setOpaque(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(fonte);
        area.setForeground(cor);
        return area;
    }

    private static final class ListaCartoes extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visivel, int orientacao, int direcao) {
            return 18;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visivel, int orientacao, int direcao) {
            return Math.max(18, visivel.height - 18);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}
