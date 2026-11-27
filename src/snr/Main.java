package snr;

import javax.swing.*;
import snr.data.DataStore;
import snr.model.HelpdeskException;
import snr.ui.AppContext;
import snr.ui.LoginFrame;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            try {
                AppContext app = new AppContext(new DataStore());
                new LoginFrame(app).setVisible(true);
            } catch (HelpdeskException e) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Cannot connect to database",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
