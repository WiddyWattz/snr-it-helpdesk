# SnR IT Helpdesk & Asset Tracking System

Java Swing desktop app. CSIT228 · Group SnR · G01

## Run
- IDE: open the folder, mark `src` as the source root, run `snr.Main`.
- Command line (JDK 17+):
  ```
  javac -d out $(find src -name "*.java")
  java -cp out snr.Main
  ```

## Accounts
- Reporter: `ana` / `ana123`, `santos` / `santos123`
- Technician: `carlo` / `tech123`, `jessa` / `tech123`
- IT Admin: `admin` / `admin123`

## Screens
- **Login** – opens the screens for the user's role.
- **Reporter** – Submit Ticket, My Tickets.
- **Technician** – Assigned Tickets.
- **IT Admin** – Dashboard (counts + overdue tickets), All Tickets, Devices.

Buttons marked "Coming soon" are not connected yet; the services behind them already exist.

## Structure
```
src/snr/
  model/    entities (User → Reporter, Technician, ITAdmin; Ticket → HardwareTicket,
            SoftwareTicket, NetworkTicket; Device; RepairNote; enums; exceptions)
  service/  business rules (AuthService, TicketService, DeviceService, ReportService)
  data/     DataStore (in-memory; replace with Supabase in Part 5)
  util/     Dates
  ui/       screens + AppContext
```
