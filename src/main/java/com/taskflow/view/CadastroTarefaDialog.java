package com.taskflow.view;

import com.taskflow.model.Status;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxUI;

final class CadastroTarefaDialog extends JDialog {
    private static final int MARGEM_CAMPO = 12;

    CadastroTarefaDialog(JFrame janela, TaskFlowFrame.CriarTarefa criarTarefa) {
        super(janela, "Nova tarefa", true);
        setUndecorated(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        var titulo = new JTextField(37) {
            @Override
            protected void paintComponent(Graphics g) {
                Tema.preencher(g, getBackground(), getWidth(), getHeight(), 12);
                super.paintComponent(g);
                if (getDocument().getLength() == 0) {
                    Tema.desenharDica(g, this, "Digite o título da tarefa", true);
                }
            }
        };
        var descricao = new JTextArea(3, 37) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getDocument().getLength() == 0) {
                    Tema.desenharDica(g, this, "Descreva a tarefa (opcional)", false);
                }
            }
        };
        titulo.setFont(Tema.TEXTO);
        descricao.setFont(Tema.TEXTO);
        descricao.setForeground(Tema.TEXTO_PRINCIPAL);
        titulo.setForeground(Tema.TEXTO_PRINCIPAL);
        descricao.setLineWrap(true);
        descricao.setWrapStyleWord(true);
        descricao.setMargin(new Insets(10, MARGEM_CAMPO, 10, MARGEM_CAMPO));
        descricao.setOpaque(false);
        titulo.setOpaque(false);
        var rolagem = new JScrollPane(descricao, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        rolagem.getViewport().setOpaque(false);
        rolagem.setOpaque(false);
        var status = new JComboBox<>(Status.values());
        status.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                var seta = new JButton(new Tema.Simbolo(Tema.Glifo.ABAIXO, Tema.SECUNDARIA, 17));
                seta.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                seta.setContentAreaFilled(false);
                seta.setFocusable(false);
                return seta;
            }
        });
        status.setSelectedItem(Status.A_FAZER);
        status.setFont(Tema.TEXTO);
        status.setForeground(Tema.TEXTO_PRINCIPAL);
        atualizarBorda(status, status, 4, 8, false);
        acompanharFoco(status, status, 4, 8, null);

        var mensagem = new JLabel(" ");
        mensagem.setForeground(Tema.EXCLUIR);
        mensagem.setFont(Tema.TEXTO.deriveFont(12f));
        mensagem.setIconTextGap(6);
        atualizarBorda(titulo, titulo, 9, MARGEM_CAMPO, false);
        acompanharFoco(titulo, titulo, 9, MARGEM_CAMPO, mensagem);
        atualizarBorda(descricao, rolagem, 0, 0, false);
        acompanharFoco(descricao, rolagem, 0, 0, null);
        titulo.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                limparMensagem();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                limparMensagem();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                limparMensagem();
            }

            private void limparMensagem() {
                if (mensagem.getIcon() != null) {
                    mensagem.setText(" ");
                    mensagem.setIcon(null);
                    atualizarBorda(titulo, titulo, 9, MARGEM_CAMPO, false);
                }
            }
        });

        var campos = new JPanel(new BorderLayout(0, 16));
        campos.setOpaque(false);
        var linhaTitulo = new JPanel(new BorderLayout(0, 8));
        linhaTitulo.setOpaque(false);
        linhaTitulo.add(titulo, BorderLayout.NORTH);
        linhaTitulo.add(mensagem, BorderLayout.CENTER);
        campos.add(campo("Título *", titulo, linhaTitulo), BorderLayout.NORTH);
        campos.add(campo("Descrição", descricao, rolagem), BorderLayout.CENTER);
        campos.add(campo("Status inicial", status, status), BorderLayout.SOUTH);

        var cancelar = Tema.botao("Cancelar", Tema.SUPERFICIE, Tema.TEXTO_PRINCIPAL);
        cancelar.addActionListener(e -> dispose());
        var criar = Tema.botao("Criar tarefa", Tema.DESTAQUE, Tema.SUPERFICIE);
        criar.setIcon(new Tema.Simbolo(Tema.Glifo.MARCA, Tema.SUPERFICIE, 19));
        criar.addActionListener(e -> {
            try {
                criarTarefa.criar(titulo.getText(), descricao.getText(), (Status) status.getSelectedItem());
                dispose();
            } catch (IllegalArgumentException erro) {
                mensagem.setText(erro.getMessage());
                mensagem.setIcon(new Tema.Simbolo(Tema.Glifo.ALERTA, Tema.EXCLUIR, 14));
                atualizarBorda(titulo, titulo, 9, MARGEM_CAMPO, true);
                titulo.requestFocusInWindow();
            }
        });

        var acoes = new JPanel();
        acoes.setLayout(new BoxLayout(acoes, BoxLayout.X_AXIS));
        acoes.setOpaque(false);
        acoes.add(Box.createHorizontalGlue());
        acoes.add(cancelar);
        acoes.add(Box.createHorizontalStrut(8));
        acoes.add(criar);
        acoes.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.BORDA),
                BorderFactory.createEmptyBorder(16, 0, 0, 0)));

        var formulario = Tema.painelArredondado(new BorderLayout(0, 16), Tema.SUPERFICIE, 24);
        formulario.setBorder(BorderFactory.createCompoundBorder(Tema.bordaArredondada(Tema.BORDA, 24, 0, 0),
                BorderFactory.createEmptyBorder(24, 24, 24, 24)));
        formulario.add(cabecalho(), BorderLayout.NORTH);
        formulario.add(campos, BorderLayout.CENTER);
        formulario.add(acoes, BorderLayout.SOUTH);
        setContentPane(formulario);
        getRootPane().setDefaultButton(criar);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancelar");
        getRootPane().getActionMap().put("cancelar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evento) {
                dispose();
            }
        });
        pack();
        if (getGraphicsConfiguration().getDevice().isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSPARENT)) {
            setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 24, 24));
        }
        setLocationRelativeTo(janela);
        SwingUtilities.invokeLater(titulo::requestFocusInWindow);
    }

    private JPanel cabecalho() {
        var faixa = new JPanel(new BorderLayout(16, 0));
        faixa.setOpaque(false);
        var icone = Tema.painelArredondado(new GridBagLayout(), Tema.DESTAQUE, 15);
        icone.setPreferredSize(new Dimension(42, 42));
        icone.add(new JLabel(new Tema.Simbolo(Tema.Glifo.MAIS, Tema.SUPERFICIE, 26)));
        faixa.add(icone, BorderLayout.WEST);

        var textos = new JPanel(new BorderLayout(0, 4));
        textos.setOpaque(false);
        var titulo = new JLabel("Nova tarefa");
        titulo.setForeground(Tema.TEXTO_PRINCIPAL);
        titulo.setFont(Tema.TEXTO.deriveFont(Font.BOLD, 23f));
        var subtitulo = new JLabel("Adicione uma nova tarefa ao seu quadro.");
        subtitulo.setForeground(Tema.SECUNDARIA);
        subtitulo.setFont(Tema.TEXTO);
        textos.add(titulo, BorderLayout.NORTH);
        textos.add(subtitulo, BorderLayout.CENTER);
        faixa.add(textos, BorderLayout.CENTER);

        var fechar = Tema.botao("", Tema.SUPERFICIE, Tema.SECUNDARIA);
        fechar.setIcon(new Tema.Simbolo(Tema.Glifo.FECHAR, Tema.SECUNDARIA, 17));
        fechar.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
        fechar.setToolTipText("Fechar cadastro");
        fechar.getAccessibleContext().setAccessibleName("Fechar cadastro");
        fechar.addActionListener(e -> dispose());
        var canto = new JPanel(new BorderLayout());
        canto.setOpaque(false);
        canto.add(fechar, BorderLayout.NORTH);
        faixa.add(canto, BorderLayout.EAST);
        return faixa;
    }

    private static JPanel campo(String nome, JComponent entrada, JComponent exibicao) {
        var rotulo = new JLabel(nome);
        rotulo.setLabelFor(entrada);
        rotulo.setFont(Tema.TEXTO.deriveFont(Font.BOLD));
        rotulo.setForeground(Tema.TEXTO_PRINCIPAL);
        var linha = new JPanel(new BorderLayout(0, 8));
        linha.setOpaque(false);
        linha.add(rotulo, BorderLayout.NORTH);
        linha.add(exibicao, BorderLayout.CENTER);
        return linha;
    }

    private static void acompanharFoco(JComponent entrada, JComponent exibicao,
            int vertical, int horizontal, JLabel erro) {
        entrada.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent evento) {
                atualizar();
            }

            @Override
            public void focusLost(FocusEvent evento) {
                atualizar();
            }

            private void atualizar() {
                atualizarBorda(entrada, exibicao, vertical, horizontal, erro != null && erro.getIcon() != null);
            }
        });
    }

    private static void atualizarBorda(JComponent entrada, JComponent exibicao, int vertical,
            int horizontal, boolean invalido) {
        Color cor = invalido ? Tema.EXCLUIR : entrada.hasFocus() ? Tema.DESTAQUE : Tema.BORDA;
        var borda = Tema.bordaArredondada(cor, 12, vertical, horizontal);
        entrada.setBackground(invalido ? Tema.ERRO_FUNDO : Tema.SUPERFICIE);
        exibicao.setBorder(borda);
        exibicao.repaint();
    }

}
