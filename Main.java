import control.AstaController;
import view.AstaGuiView;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Forziamo il Look & Feel moderno (Nimbus) nativo di Java su Linux
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Se Nimbus non è disponibile, usa quello di sistema
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }

        // Avvio dell'interfaccia grafica nei thread di Swing
        SwingUtilities.invokeLater(() -> {
            AstaController controller = new AstaController();
            AstaGuiView view = new AstaGuiView(controller);
            if (view.isSetupCompletato()) {
                view.setVisible(true);
            }
        });
    }
}