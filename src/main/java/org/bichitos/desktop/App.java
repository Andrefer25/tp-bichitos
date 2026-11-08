package org.bichitos.desktop;

import org.bichitos.repository.Database;
import org.bichitos.repository.DemoDatabase;
import javax.swing.*;
import java.sql.Connection;

/** Entrada de escritorio: no inicia servidor HTTP ni abre un navegador. */
public final class App {
    private App() { }
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            DemoDatabase.initialize(); // Este hilo es main; nunca el hilo de eventos de Swing.
            try(Connection db=Database.open()) { if(!db.isValid(5))throw new IllegalStateException("MySQL no responde"); }
            SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
        } catch(Exception e) {
            System.err.println("No se pudo iniciar Bichitos. Revisar MySQL y la configuración local.");
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                "No se pudo conectar con la base local. Revisá MySQL, .env y la instalación del esquema bichitos_tp4.\nLos registros existentes se conservan.",
                "Bichitos Zona Norte",JOptionPane.ERROR_MESSAGE));
        }
    }
}
