package snr.service;

import snr.model.*;

import java.time.LocalDateTime;
import java.util.*;

public class DashboardSummary {
    public record Activity(LocalDateTime time, String text, String tag, int ticketId) { }

    private final Map<Status, Integer> countByStatus = new EnumMap<>(Status.class);
    private final Map<Priority, Integer> activeByPriority = new EnumMap<>(Priority.class);
    private final Map<Category, Integer> countByCategory = new EnumMap<>(Category.class);
    private final List<Ticket> overdue = new ArrayList<>();
    private final Map<Device, Integer> ticketsPerDevice = new LinkedHashMap<>();
    private final List<Activity> activity = new ArrayList<>();
    private final int total;
    private int unassigned;

    public DashboardSummary(List<Ticket> tickets) {
        total = tickets.size();
        for (Status s : Status.values()) countByStatus.put(s, 0);
        for (Priority p : Priority.values()) activeByPriority.put(p, 0);
        for (Category c : Category.values()) countByCategory.put(c, 0);
        for (Ticket t : tickets) {
            countByStatus.merge(t.getStatus(), 1, Integer::sum);
            countByCategory.merge(t.getCategory(), 1, Integer::sum);
            ticketsPerDevice.merge(t.getDevice(), 1, Integer::sum);
            if (t.isOverdue()) overdue.add(t);
            boolean active = t.getStatus() == Status.OPEN || t.getStatus() == Status.IN_PROGRESS;
            if (active) activeByPriority.merge(t.getPriority(), 1, Integer::sum);
            if (active && t.getAssignee() == null) unassigned++;
            addActivity(t);
        }
        activity.sort(Comparator.comparing(Activity::time).reversed());
    }

    private void addActivity(Ticket t) {
        activity.add(new Activity(t.getCreatedAt(), "New " + t.getCategory().toString().toLowerCase()
                + " ticket: " + t.getDescription(), "Created", t.getId()));
        if (t.getAssignedAt() != null && t.getAssignee() != null) {
            activity.add(new Activity(t.getAssignedAt(), "Assigned to " + t.getAssignee().getFullName(), "Assigned", t.getId()));
        }
        if (t.getResolvedAt() != null) {
            activity.add(new Activity(t.getResolvedAt(), "Resolved: " + t.getDescription(), "Resolved", t.getId()));
        }
        for (RepairNote n : t.getNotes()) {
            boolean reopened = n.getNote().startsWith("Reopened:");
            String who = n.getAuthor() == null ? "Someone" : n.getAuthor().getFullName();
            activity.add(new Activity(n.getDateTime(), (reopened ? who + " reopened: " : who + ": ")
                    + n.getNote().replaceFirst("^Reopened: ", ""), reopened ? "Reopened" : "Note", t.getId()));
        }
    }

    public int getTotal() { return total; }
    public int getCount(Status status) { return countByStatus.get(status); }
    public int getActiveCount(Priority priority) { return activeByPriority.get(priority); }
    public int getCount(Category category) { return countByCategory.get(category); }
    public int getUnassignedCount() { return unassigned; }
    public int getOverdueCount() { return overdue.size(); }
    public List<Ticket> getOverdueTickets() { return Collections.unmodifiableList(overdue); }

    public int getOnTimePercent() {
        return total == 0 ? 100 : Math.round(100f * (total - overdue.size()) / total);
    }

    public List<Activity> getRecentActivity(int limit) {
        return activity.subList(0, Math.min(limit, activity.size()));
    }

    public List<Map.Entry<Device, Integer>> getTopDevices() {
        List<Map.Entry<Device, Integer>> list = new ArrayList<>(ticketsPerDevice.entrySet());
        list.sort(Map.Entry.<Device, Integer>comparingByValue().reversed());
        return list;
    }
}
