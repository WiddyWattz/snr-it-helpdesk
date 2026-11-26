# SnR IT Helpdesk & Asset Tracking System

CSIT228 Object-Oriented Programming 2 project by **Group SnR (G01)**.

A desktop app (Java Swing) where students and faculty report IT problems, the IT admin assigns
them to technicians, and every ticket is linked to a device so each device has a repair history.

## Users

| User | What they can do |
|---|---|
| Reporter (student / faculty) | Submit tickets, view their own tickets |
| Technician | View assigned tickets, update status, add repair notes |
| IT Admin | Dashboard, assign tickets, manage the device inventory |

## Current status: barebones build

- Login screen (pick a role and a user; no passwords yet)
- Role-based main window with a sidebar
- Reporter: Submit Ticket form (works with sample data), My Tickets
- Technician: Assigned Tickets
- Admin: Dashboard (counts + overdue tickets), All Tickets, Devices
- Data is sample data kept in memory (`InMemoryRepository`); buttons that are not built yet show "Coming soon"

Next: connect to Supabase (`db/schema.sql`), then build assign / update status / repair notes / device editing.

## Project structure

```
src/com/snr/helpdesk/
├── Main.java
├── model/   Ticket (abstract) → HardwareTicket, SoftwareTicket, NetworkTicket
│            Device, User, enums: Role, Status, Priority, Category, DeviceType
├── data/    HelpdeskRepository (interface), InMemoryRepository
└── ui/      LoginFrame, MainFrame, DashboardPanel, TicketsPanel,
             SubmitTicketPanel, DevicesPanel, Theme
db/schema.sql   tables for Supabase
```

## How to run

Requires JDK 17 or newer.

- **VS Code:** install the *Extension Pack for Java*, open this folder, open `Main.java` and click **Run**.
- **Linux / macOS:** `./run.sh`
- **Windows:** double-click `run.bat`

## Database (later)

1. Create a Supabase project and run `db/schema.sql` in the SQL Editor.
2. Copy `config.example.properties` to `config.properties` and fill in the project URL and anon key.
   `config.properties` is in `.gitignore`, so the key is never pushed.
