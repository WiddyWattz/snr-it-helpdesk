package snr.model;

import java.time.LocalDateTime;

public class NetworkTicket extends Ticket {
    public NetworkTicket(int id, String description, Priority priority, Device device,
                      Reporter reporter, LocalDateTime createdAt) {
        super(id, description, priority, device, reporter, createdAt);
    }

    @Override
    public Category getCategory() { return Category.NETWORK; }

    // Network issues affect many users: half the time limit.
    @Override
    public int getSlaHours() { return Math.max(1, super.getSlaHours() / 2); }
}
