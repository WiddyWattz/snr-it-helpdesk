package snr.service;

import snr.data.DataStore;
import snr.model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TicketService {
    private final DataStore store;

    public TicketService(DataStore store) { this.store = store; }

    public Ticket submit(Reporter reporter, Category category, Priority priority,
                         Device device, String description) throws HelpdeskException {
        if (category == null || priority == null) throw new HelpdeskException("Choose a category and priority.");
        if (device == null) throw new HelpdeskException("Choose the affected device.");
        if (description == null || description.trim().isEmpty()) throw new HelpdeskException("Describe the problem.");

        int id = store.generateTicketId();
        LocalDateTime now = LocalDateTime.now();
        String text = description.trim();
        Ticket ticket;
        switch (category) {
            case HARDWARE: ticket = new HardwareTicket(id, text, priority, device, reporter, now); break;
            case SOFTWARE: ticket = new SoftwareTicket(id, text, priority, device, reporter, now); break;
            default:       ticket = new NetworkTicket(id, text, priority, device, reporter, now); break;
        }
        store.saveTicket(ticket);
        return ticket;
    }

    // Admin: all, reporter: own, technician: assigned.
    public List<Ticket> getTicketsFor(User user) {
        List<Ticket> list = new ArrayList<>();
        for (Ticket t : store.getAllTickets()) {
            if (user instanceof ITAdmin
                    || t.getReporter() == user
                    || t.getAssignee() == user) {
                list.add(t);
            }
        }
        list.sort(Comparator.comparing(Ticket::getCreatedAt).reversed());
        return list;
    }

    public List<Ticket> getUnassigned() {
        List<Ticket> list = new ArrayList<>();
        for (Ticket t : store.getAllTickets()) {
            if (t.getAssignee() == null) list.add(t);
        }
        list.sort(Comparator.comparing((Ticket t) -> t.getPriority().ordinal()).reversed()
                .thenComparing(Ticket::getCreatedAt));
        return list;
    }

    public List<Technician> getTechnicians() { return store.getTechnicians(); }

    public int countActiveTickets(Technician technician) {
        int count = 0;
        for (Ticket t : store.getAllTickets()) {
            if (t.getAssignee() == technician && t.getStatus() != Status.RESOLVED
                    && t.getStatus() != Status.CLOSED) count++;
        }
        return count;
    }

    public void assign(Ticket ticket, Technician technician) throws HelpdeskException {
        if (technician == null) throw new HelpdeskException("Choose a technician.");
        if (ticket.getStatus() == Status.CLOSED) throw new HelpdeskException("This ticket is already closed.");
        ticket.assignTo(technician, LocalDateTime.now());
    }

    public void changeStatus(Ticket ticket, Status next) throws HelpdeskException {
        if (ticket.getAssignee() == null) throw new HelpdeskException("Assign a technician first.");
        ticket.moveTo(next, LocalDateTime.now());
    }

    public void addNote(Ticket ticket, Technician technician, String note) throws HelpdeskException {
        if (note == null || note.trim().isEmpty()) throw new HelpdeskException("The note is empty.");
        ticket.addNote(new RepairNote(technician, note.trim(), LocalDateTime.now()));
    }
}
