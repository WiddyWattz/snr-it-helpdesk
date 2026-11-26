package snr.ui;

import snr.data.DataStore;
import snr.service.AuthService;
import snr.service.DeviceService;
import snr.service.ReportService;
import snr.service.TicketService;

public class AppContext {
    private final AuthService authService;
    private final TicketService ticketService;
    private final DeviceService deviceService;
    private final ReportService reportService;

    public AppContext(DataStore store) {
        authService = new AuthService(store);
        ticketService = new TicketService(store);
        deviceService = new DeviceService(store);
        reportService = new ReportService(store);
    }

    public AuthService auth() { return authService; }
    public TicketService tickets() { return ticketService; }
    public DeviceService devices() { return deviceService; }
    public ReportService reports() { return reportService; }
}
