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

        return store.insertTicket(category, priority, device, reporter, description.trim(), LocalDateTime.now());
    }

    // Admin: all, reporter: own, technician: assigned.
    public List<Ticket> getTicketsFor(User user) {
        List<Ticket> list = new ArrayList<>();
        for (Ticket t : store.getAllTickets()) {
            if (user instanceof ITAdmin
                    || user.equals(t.getReporter())
                    || user.equals(t.getAssignee())) {
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
            if (technician.equals(t.getAssignee()) && t.getStatus() != Status.RESOLVED
                    && t.getStatus() != Status.CLOSED) count++;
        }
        return count;
    }

    public void assign(Ticket ticket, Technician technician) throws HelpdeskException {
        if (technician == null) throw new HelpdeskException("Choose a technician.");
        if (ticket.getStatus() == Status.RESOLVED || ticket.getStatus() == Status.CLOSED) {
            throw new HelpdeskException("This ticket is already " + ticket.getStatus() + ".");
        }
        ticket.assignTo(technician, LocalDateTime.now());
        store.updateTicket(ticket);
    }

    public void changeStatus(Ticket ticket, Technician technician, Status next) throws HelpdeskException {
        checkAssignedTo(ticket, technician);
        if (next == null) throw new HelpdeskException("Choose a status.");
        if (!ticket.getStatus().canMoveTo(next)) throw new InvalidStatusChangeException(ticket.getStatus(), next);
        if (next == Status.RESOLVED && ticket.getNotes().isEmpty()) {
            throw new HelpdeskException("Add a repair note before resolving.");
        }
        ticket.moveTo(next, LocalDateTime.now());
        store.updateTicket(ticket);
    }

    public void addNote(Ticket ticket, Technician technician, String note) throws HelpdeskException {
        checkAssignedTo(ticket, technician);
        if (ticket.getStatus() == Status.CLOSED) throw new HelpdeskException("This ticket is already closed.");
        if (note == null || note.trim().isEmpty()) throw new HelpdeskException("The note is empty.");
        RepairNote repairNote = new RepairNote(technician, note.trim(), LocalDateTime.now());
        store.insertNote(ticket, repairNote);
        ticket.addNote(repairNote);
    }

    private void checkAssignedTo(Ticket ticket, Technician technician) throws HelpdeskException {
        if (ticket.getAssignee() == null) throw new HelpdeskException("Assign a technician first.");
        if (!ticket.getAssignee().equals(technician)) throw new HelpdeskException("This ticket is not assigned to you.");
    }
}
