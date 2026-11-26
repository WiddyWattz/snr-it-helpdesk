package snr.service;

import snr.data.DataStore;

public class ReportService {
    private final DataStore store;

    public ReportService(DataStore store) { this.store = store; }

    public DashboardSummary getDashboard() {
        return new DashboardSummary(store.getAllTickets());
    }
}
