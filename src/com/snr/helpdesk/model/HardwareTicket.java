package com.snr.helpdesk.model;

import java.time.LocalDateTime;

public class HardwareTicket extends Ticket {

    public HardwareTicket(int id, String description, Priority priority, Device device,
                      User reporter, LocalDateTime createdAt) {
        super(id, description, priority, device, reporter, createdAt);
    }

    @Override
    public Category getCategory() {
        return Category.HARDWARE;
    }
}
