package com.snr.helpdesk.model;

import java.time.LocalDateTime;

public class NetworkTicket extends Ticket {

    public NetworkTicket(int id, String description, Priority priority, Device device,
                      User reporter, LocalDateTime createdAt) {
        super(id, description, priority, device, reporter, createdAt);
    }

    @Override
    public Category getCategory() {
        return Category.NETWORK;
    }

    /** Network problems affect many users, so they get half the normal time. */
    @Override
    public int getSlaHours() {
        return Math.max(1, super.getSlaHours() / 2);
    }
}
