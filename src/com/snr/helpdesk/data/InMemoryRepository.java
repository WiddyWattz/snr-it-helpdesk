package com.snr.helpdesk.data;

import com.snr.helpdesk.model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Sample data held in memory so the GUI can be shown before the database is connected. */
public class InMemoryRepository implements HelpdeskRepository {
    private final List<User> users = new ArrayList<>();
    private final List<Device> devices = new ArrayList<>();
    private final List<Ticket> tickets = new ArrayList<>();

    public InMemoryRepository() {
        User ana = new User(1, "Ana Reyes", Role.REPORTER);
        User prof = new User(2, "Prof. Santos", Role.REPORTER);
        User tech1 = new User(3, "Carlo Lim", Role.TECHNICIAN);
        User tech2 = new User(4, "Jessa Uy", Role.TECHNICIAN);
        User admin = new User(5, "Admin Cruz", Role.ADMIN);
        users.addAll(List.of(ana, prof, tech1, tech2, admin));

        Device pc12 = new Device("PC-12", DeviceType.PC, "Lab 3", "Working");
        Device pc07 = new Device("PC-07", DeviceType.PC, "Lab 3", "Needs repair");
        Device prn1 = new Device("PRN-01", DeviceType.PRINTER, "Faculty Room", "Working");
        Device rtr2 = new Device("RTR-02", DeviceType.ROUTER, "2nd Floor", "Working");
        devices.addAll(List.of(pc12, pc07, prn1, rtr2));

        LocalDateTime now = LocalDateTime.now();
        Ticket t1 = new HardwareTicket(1, "PC won't turn on", Priority.HIGH, pc07, ana, now.minusHours(6));
        t1.setAssignee(tech1);
        Ticket t2 = new NetworkTicket(2, "No internet on 2nd floor", Priority.MEDIUM, rtr2, prof, now.minusHours(3));
        t2.setAssignee(tech2);
        t2.moveTo(Status.IN_PROGRESS);
        Ticket t3 = new SoftwareTicket(3, "NetBeans missing on PC-12", Priority.LOW, pc12, ana, now.minusHours(1));
        Ticket t4 = new HardwareTicket(4, "Printer paper jam", Priority.MEDIUM, prn1, prof, now.minusDays(2));
        t4.setAssignee(tech1);
        t4.moveTo(Status.IN_PROGRESS);
        t4.moveTo(Status.RESOLVED);
        tickets.addAll(List.of(t1, t2, t3, t4));
    }

    @Override
    public List<User> getUsers(Role role) {
        return users.stream().filter(u -> u.getRole() == role).collect(Collectors.toList());
    }

    @Override
    public List<Device> getDevices() {
        return new ArrayList<>(devices);
    }

    @Override
    public List<Ticket> getTickets() {
        return new ArrayList<>(tickets);
    }

    @Override
    public List<Ticket> getTicketsByReporter(User reporter) {
        return tickets.stream().filter(t -> t.getReporter().getId() == reporter.getId())
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> getTicketsByAssignee(User technician) {
        return tickets.stream()
                .filter(t -> t.getAssignee() != null && t.getAssignee().getId() == technician.getId())
                .collect(Collectors.toList());
    }

    @Override
    public void addTicket(Ticket ticket) {
        tickets.add(ticket);
    }

    @Override
    public int nextTicketId() {
        return tickets.stream().mapToInt(Ticket::getId).max().orElse(0) + 1;
    }
}
