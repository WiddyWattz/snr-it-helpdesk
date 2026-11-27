package snr.ui;

import snr.data.DataStore;
import snr.model.HelpdeskException;
import snr.service.AuthService;
import snr.service.DeviceService;
import snr.service.ReportService;
import snr.service.TicketService;
import snr.service.UserService;

public class AppContext {
    private final AuthService authService;
    private final TicketService ticketService;
    private final DeviceService deviceService;
    private final ReportService reportService;
    private final UserService userService;
    private final DataStore store;

    public AppContext(DataStore store) {
        this.store = store;
        authService = new AuthService(store);
        ticketService = new TicketService(store);
        deviceService = new DeviceService(store);
        reportService = new ReportService(store);
        userService = new UserService(store);
    }

    public AuthService auth() { return authService; }
    public TicketService tickets() { return ticketService; }
    public DeviceService devices() { return deviceService; }
    public ReportService reports() { return reportService; }
    public UserService users() { return userService; }

    public void reload() throws HelpdeskException { store.reload(); }
}
