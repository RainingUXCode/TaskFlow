package com.taskflow.view;

import com.taskflow.model.Status;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GradientPaint;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.util.Arrays;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;

final class Tema {
    static final Color MARINHO = new Color(0x14264B);
    static final Color TEXTO_PRINCIPAL = new Color(0x172B4D);
    static final Color SECUNDARIA = new Color(0x52647D);
    static final Color FUNDO = new Color(0xF8FAFC);
    static final Color FUNDO_AZUL = new Color(0xF1F5F9);
    static final Color MARINHO_ESCURO = new Color(0x0C1731);
    static final Color BORDA = new Color(0xE2E8F0);
    static final Color DESTAQUE = new Color(0x215CEB);
    static final Color AZUL_CLARO = new Color(0x306BE0);
    static final Color SUPERFICIE = Color.WHITE;
    static final Color CHIP = new Color(0x304360);
    static final Color TEXTO_CLARO = new Color(0xDCE6F7);
    static final Color EXCLUIR = new Color(0xA92736);
    static final Color ERRO_FUNDO = new Color(0xFFF3F4);
    static final Color SUCESSO = new Color(0x16A36E);
    static final int ESPACO = 8;
    static final int RAIO = 22;
    static final Font TEXTO = fonteInterface();

    private Tema() {
    }

    private static Font fonteInterface() {
        boolean temSegoe = Arrays.stream(GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames()).anyMatch("Segoe UI"::equalsIgnoreCase);
        if (temSegoe) {
            return new Font("Segoe UI", Font.PLAIN, 14);
        }
        Font fonte = UIManager.getFont("Label.font");
        return fonte == null ? new Font(Font.SANS_SERIF, Font.PLAIN, 14) : fonte.deriveFont(Font.PLAIN, 14f);
    }

    static void desenharDica(Graphics g, JComponent campo, String texto, boolean centralizarVerticalmente) {
        var desenho = (Graphics2D) g.create();
        desenho.setFont(campo.getFont());
        desenho.setColor(SECUNDARIA);
        desenho.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        // LCD usa a ordem física dos subpixels e só deve ser solicitado na tela.
        if (desenho.getDeviceConfiguration().getDevice().getType() == GraphicsDevice.TYPE_RASTER_SCREEN
                && Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints") instanceof Map<?, ?> hints) {
            Object antialias = hints.get(RenderingHints.KEY_TEXT_ANTIALIASING);
            if (antialias != null && antialias != RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT
                    && antialias != RenderingHints.VALUE_TEXT_ANTIALIAS_OFF) {
                desenho.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, antialias);
            }
            Object contraste = hints.get(RenderingHints.KEY_TEXT_LCD_CONTRAST);
            if (contraste != null) {
                desenho.setRenderingHint(RenderingHints.KEY_TEXT_LCD_CONTRAST, contraste);
            }
        }
        var margens = campo.getInsets();
        var metricas = desenho.getFontMetrics();
        int altura = campo.getHeight() - margens.top - margens.bottom;
        int topo = centralizarVerticalmente ? margens.top + (altura - metricas.getHeight()) / 2 : margens.top;
        desenho.drawString(texto, margens.left, topo + metricas.getAscent());
        desenho.dispose();
    }

    static Border bordaCartao(int margem) {
        return bordaArredondada(BORDA, RAIO, margem, margem);
    }

    static Border bordaArredondada(Color cor, int arco, int vertical, int horizontal) {
        return BorderFactory.createCompoundBorder(
                new AbstractBorder() {
                    @Override
                    public void paintBorder(Component c, Graphics g, int x, int y, int largura, int altura) {
                        var desenho = (Graphics2D) g.create();
                        desenho.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        desenho.setColor(cor);
                        desenho.drawRoundRect(x, y, largura - 1, altura - 1, arco, arco);
                        desenho.dispose();
                    }
                },
                BorderFactory.createEmptyBorder(vertical, horizontal, vertical, horizontal));
    }

    static Icon selo(Glifo glifo, Color cor, int tamanho) {
        return new Icon() {
            @Override
            public int getIconWidth() {
                return tamanho;
            }

            @Override
            public int getIconHeight() {
                return tamanho;
            }

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                new Simbolo(Glifo.PONTO, cor, tamanho).paintIcon(c, g, x, y);
                int margem = tamanho / 5;
                new Simbolo(glifo, SUPERFICIE, tamanho - margem * 2).paintIcon(c, g, x + margem, y + margem);
            }
        };
    }

    static Color corStatus(Status status) {
        return switch (status) {
            case A_FAZER -> new Color(0x3B6FF4);
            case EM_ANDAMENTO -> new Color(0xEC8E26);
            case CONCLUIDO -> SUCESSO;
        };
    }

    static Color tonalizar(Color base, Color cor, float intensidade) {
        float restante = 1 - intensidade;
        return new Color(Math.round(base.getRed() * restante + cor.getRed() * intensidade),
                Math.round(base.getGreen() * restante + cor.getGreen() * intensidade),
                Math.round(base.getBlue() * restante + cor.getBlue() * intensidade));
    }

    static JPanel painelGradiente(java.awt.LayoutManager layout, Color inicio, Color fim, boolean diagonal) {
        return painelGradiente(layout, inicio, fim, diagonal, 0);
    }

    static JPanel painelGradiente(java.awt.LayoutManager layout, Color inicio, Color fim, boolean diagonal, int arco) {
        var painel = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics g) {
                gradiente(g, inicio, fim, getWidth(), getHeight(), arco, diagonal);
                super.paintComponent(g);
            }
        };
        painel.setOpaque(false);
        return painel;
    }

    static void gradiente(Graphics g, Color inicio, Color fim, int largura, int altura, int arco, boolean diagonal) {
        if (largura < 1 || altura < 1) {
            return;
        }
        var d = (Graphics2D) g.create();
        d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        d.setPaint(new GradientPaint(0, 0, inicio, diagonal ? largura : 0, altura, fim));
        d.fillRoundRect(0, 0, largura, altura, arco, arco);
        d.dispose();
    }

    static void sombra(Graphics g, int largura, int altura, int arco) {
        var d = (Graphics2D) g.create();
        d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        d.setColor(new Color(19, 39, 80, 10));
        d.fillRoundRect(0, 2, largura, altura + 3, arco, arco);
        d.setColor(new Color(19, 39, 80, 12));
        d.fillRoundRect(1, 1, largura - 2, altura + 2, arco, arco);
        d.dispose();
    }

    static JPanel painelArredondado(java.awt.LayoutManager layout, Color fundo, int arco) {
        var painel = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics g) {
                preencher(g, fundo, getWidth(), getHeight(), arco);
                super.paintComponent(g);
            }
        };
        painel.setOpaque(false);
        return painel;
    }

    static void preencher(Graphics g, Color cor, int largura, int altura, int arco) {
        var desenho = (Graphics2D) g.create();
        desenho.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        desenho.setColor(cor);
        desenho.fillRoundRect(0, 0, largura, altura, arco, arco);
        desenho.dispose();
    }

    enum Glifo { PONTO, QUADRO, MAIS, ESQUERDA, DIREITA, ABAIXO, LIXEIRA, MARCA, FECHAR, LISTA, RAIO, ALERTA, BUSCA }

    record Simbolo(Glifo glifo, Color cor, int tamanho) implements Icon {
        @Override
        public int getIconWidth() {
            return tamanho;
        }

        @Override
        public int getIconHeight() {
            return tamanho;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            var d = (Graphics2D) g.create();
            d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            d.translate(x, y);
            d.scale(tamanho / 20.0, tamanho / 20.0);
            d.setColor(cor);
            d.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            switch (glifo) {
                case PONTO -> d.fillOval(0, 0, 20, 20);
                case QUADRO -> {
                    d.drawPolygon(new int[]{1, 10, 19, 10}, new int[]{6, 1, 6, 11}, 4);
                    d.drawPolyline(new int[]{1, 10, 19}, new int[]{10, 15, 10}, 3);
                    d.drawPolyline(new int[]{1, 10, 19}, new int[]{14, 19, 14}, 3);
                }
                case MAIS -> {
                    d.drawLine(4, 10, 16, 10);
                    d.drawLine(10, 4, 10, 16);
                }
                case ESQUERDA, DIREITA -> {
                    d.drawLine(3, 10, 17, 10);
                    int ponta = glifo == Glifo.ESQUERDA ? 3 : 17;
                    d.drawLine(10, 4, ponta, 10);
                    d.drawLine(10, 16, ponta, 10);
                }
                case ABAIXO -> d.drawPolyline(new int[]{4, 10, 16}, new int[]{7, 13, 7}, 3);
                case LIXEIRA -> {
                    d.drawRoundRect(5, 6, 10, 12, 2, 2);
                    d.drawLine(3, 6, 17, 6);
                    d.drawLine(8, 3, 12, 3);
                    d.drawLine(8, 9, 8, 15);
                    d.drawLine(12, 9, 12, 15);
                }
                case MARCA -> {
                    d.drawLine(5, 10, 8, 13);
                    d.drawLine(8, 13, 15, 6);
                }
                case FECHAR -> {
                    d.drawLine(4, 4, 16, 16);
                    d.drawLine(4, 16, 16, 4);
                }
                case LISTA -> {
                    d.drawRoundRect(4, 3, 12, 15, 2, 2);
                    d.drawLine(7, 7, 13, 7);
                    d.drawLine(7, 11, 13, 11);
                    d.drawLine(7, 15, 11, 15);
                }
                case RAIO -> d.fillPolygon(new int[]{12, 3, 9, 7, 17, 11}, new int[]{1, 11, 11, 19, 8, 8}, 6);
                case ALERTA -> {
                    d.fillOval(0, 0, 20, 20);
                    d.setColor(SUPERFICIE);
                    d.drawLine(10, 5, 10, 11);
                    d.fillOval(9, 13, 2, 2);
                }
                case BUSCA -> {
                    d.drawOval(2, 2, 11, 11);
                    d.drawLine(11, 11, 17, 17);
                }
            }
            d.dispose();
        }
    }

    static JButton botao(String texto, Color fundo, Color cor) {
        var botao = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                var desenho = (Graphics2D) g.create();
                desenho.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                var modelo = getModel();
                boolean primario = fundo.equals(DESTAQUE) || fundo.equals(MARINHO);
                Color base = modelo.isPressed() ? fundo.darker() : modelo.isRollover()
                        ? tonalizar(fundo, DESTAQUE, .08f) : fundo;
                desenho.setPaint(fundo.equals(DESTAQUE) && !modelo.isPressed()
                        ? new GradientPaint(0, 0, AZUL_CLARO, 0, Math.max(1, getHeight()), base) : base);
                desenho.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                desenho.setColor(primario ? fundo : cor.equals(EXCLUIR)
                        ? tonalizar(BORDA, EXCLUIR, .18f) : BORDA);
                desenho.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                if (isFocusOwner()) {
                    desenho.setColor(primario ? SUPERFICIE : DESTAQUE);
                    desenho.drawRoundRect(3, 3, getWidth() - 7, getHeight() - 7, 7, 7);
                }
                desenho.dispose();
                super.paintComponent(g);
            }
        };
        botao.setFont(TEXTO.deriveFont(Font.BOLD, 13f));
        botao.setForeground(cor);
        botao.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        botao.setContentAreaFilled(false);
        botao.setFocusPainted(false);
        botao.setRolloverEnabled(true);
        botao.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        return botao;
    }
}
