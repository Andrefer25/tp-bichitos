package org.bichitos.desktop;
import javax.swing.*;
import java.awt.*;
/** Arranca la clase principal real contra bichitos_tp4 y cierra solo su ventana. */
public final class StartupSmokeTest {
    public static void main(String[] args)throws Exception {
        App.main(new String[0]);
        final boolean[] opened={false};
        SwingUtilities.invokeAndWait(()->{for(Frame f:Frame.getFrames())if(f instanceof MainFrame&&f.isVisible())opened[0]=true;});
        if(!opened[0])throw new AssertionError("La clase App no abrió la ventana principal");
        Thread.sleep(700);
        SwingUtilities.invokeAndWait(()->{for(Frame f:Frame.getFrames())if(f instanceof MainFrame)f.dispose();});
        System.out.println("OK: org.bichitos.desktop.App abre la ventana real con MySQL local y reset desactivado.");
    }
}
