package com.snr.helpdesk;

import com.snr.helpdesk.data.InMemoryRepository;
import com.snr.helpdesk.ui.LoginFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
            // fall back to the default look and feel
        }
        SwingUtilities.invokeLater(() -> new LoginFrame(new InMemoryRepository()).setVisible(true));
    }
}
