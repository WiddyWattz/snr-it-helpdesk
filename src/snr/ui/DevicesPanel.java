package snr.ui;

import snr.model.Device;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class DevicesPanel extends JPanel {

    public DevicesPanel(AppContext app) {
        super(new BorderLayout());
        DefaultTableModel model = new DefaultTableModel(new String[]{"Code", "Type", "Location", "Condition"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (Device d : app.devices().getAllDevices()) {
            model.addRow(new Object[]{d.getCode(), d.getType(), d.getLocation(), d.getCondition()});
        }
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(Theme.BODY);
        table.getTableHeader().setFont(Theme.HEADING.deriveFont(13f));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 12));
        actions.setOpaque(false);
        for (String label : new String[]{"Add Device", "Edit Device", "Repair History"}) {
            JButton button = Theme.primaryButton(label);
            button.addActionListener(e -> Theme.comingSoon(this, label));
            actions.add(button);
            actions.add(Box.createHorizontalStrut(8));
        }

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Theme.CARD);
        body.add(scroll, BorderLayout.CENTER);
        body.add(actions, BorderLayout.SOUTH);
        add(Theme.page("Device Inventory", body), BorderLayout.CENTER);
    }
}
