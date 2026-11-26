package snr;

import snr.data.DataStore;
import snr.ui.AppContext;
import snr.ui.LoginFrame;

import javax.swing.*;

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
