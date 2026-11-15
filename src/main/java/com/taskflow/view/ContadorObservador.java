package com.taskflow.view;

import com.taskflow.model.Status;
import com.taskflow.observer.EventoTarefa;
import com.taskflow.observer.Observador;
import java.awt.Font;
import java.awt.BorderLayout;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JComponent;

public final class ContadorObservador implements Observador {
    private final Map<Status, JLabel> valores = new EnumMap<>(Status.class);
    private final Map<Status, JComponent> badges = new EnumMap<>(Status.class);

    public ContadorObservador() {
        for (Status status : Status.values()) {
            var valor = new JLabel("0");
            valor.setFont(Tema.TEXTO.deriveFont(Font.BOLD, 13f));
            valor.setForeground(Tema.TEXTO_PRINCIPAL);
            valor.getAccessibleContext().setAccessibleName("Total de tarefas em " + status);
            var fundo = Tema.tonalizar(Tema.FUNDO, Tema.corStatus(status), .14f);
            var numero = Tema.painelArredondado(new BorderLayout(), fundo, 20);
            numero.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            numero.setToolTipText("Total de tarefas em " + status);
            numero.add(valor);
            valores.put(status, valor);
            badges.put(status, numero);
        }
    }

    public JComponent componente(Status status) {
        return badges.get(status);
    }

    @Override
    public void atualizar(EventoTarefa evento) {
        Map<Status, Integer> totais = new EnumMap<>(Status.class);
        for (var tarefa : evento.tarefas()) {
            totais.merge(tarefa.status(), 1, Integer::sum);
        }
        valores.forEach((status, valor) -> valor.setText(totais.getOrDefault(status, 0).toString()));
    }
}
