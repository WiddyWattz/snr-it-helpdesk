package snr.model;

import java.time.LocalDateTime;

public class SoftwareTicket extends Ticket {
    public SoftwareTicket(int id, String description, Priority priority, Device device,
                      Reporter reporter, LocalDateTime createdAt) {
        super(id, description, priority, device, reporter, createdAt);
    }

    @Override
    public Category getCategory() { return Category.SOFTWARE; }
}
