package snr;

import javax.swing.*;
import snr.data.DataStore;
import snr.ui.AppContext;
import snr.ui.LoginFrame;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            AppContext app = new AppContext(new DataStore());
            new LoginFrame(app).setVisible(true);
        });
    }
}