package snr;

import javax.swing.*;
import snr.data.DataStore;
import snr.model.HelpdeskException;
import snr.ui.AppContext;
import snr.ui.LoginFrame;
import snr.ui.Theme;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Theme.install();
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
