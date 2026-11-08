package org.bichitos.desktop;

import javax.swing.*;
import java.awt.*;

/** Contrato común de las seis vistas. Cada vista carga sus propios datos. */
public abstract class WorkspacePanel extends JPanel {
    protected final MainFrame frame;
    protected WorkspacePanel(MainFrame frame) {
        super(new BorderLayout(10,10)); this.frame=frame;
        setBorder(BorderFactory.createEmptyBorder(12,16,12,16));
        setBackground(new Color(248,249,246));
    }
    public abstract void refreshData();
}
