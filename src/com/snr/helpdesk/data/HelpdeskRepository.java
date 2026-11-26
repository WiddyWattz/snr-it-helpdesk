package com.snr.helpdesk.data;

import com.snr.helpdesk.model.Device;
import com.snr.helpdesk.model.Role;
import com.snr.helpdesk.model.Ticket;
import com.snr.helpdesk.model.User;

import java.util.List;

/**
 * Everything the screens need from storage. The in-memory version is used for the
 * barebones build; a Supabase version will implement this same interface later.
 */
public interface HelpdeskRepository {
    List<User> getUsers(Role role);
    List<Device> getDevices();
    List<Ticket> getTickets();
    List<Ticket> getTicketsByReporter(User reporter);
    List<Ticket> getTicketsByAssignee(User technician);
    void addTicket(Ticket ticket);
    int nextTicketId();
}
