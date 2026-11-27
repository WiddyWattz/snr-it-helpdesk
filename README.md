# SnR IT Helpdesk & Asset Tracking System

Java Swing desktop app. CSIT228 · Group SnR · G01

## Run
1. Create `config.properties` in the project folder (ask the group for the values):
   ```
   supabase.url=https://YOUR-PROJECT.supabase.co
   supabase.key=YOUR-PUBLISHABLE-KEY
   ```
   It is in `.gitignore`, so it is never pushed.
2. IDE: open the folder, mark `src` as the source root, run `snr.Main`.
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

## Structure
```
src/snr/
  model/    entities (User → Reporter, Technician, ITAdmin; Ticket → HardwareTicket,
            SoftwareTicket, NetworkTicket; Device; RepairNote; enums; exceptions)
  service/  business rules (AuthService, TicketService, DeviceService, ReportService)
  data/     DataStore (loads/saves through Supabase), SupabaseClient (REST API)
  util/     Dates, Json
  ui/       screens + AppContext
```

## Database (Supabase)
The app uses the Supabase REST API with the project's publishable key. To set up a new project,
run this once in the Supabase SQL Editor:

<details>
<summary>Schema + sample data</summary>

```sql
-- SnR IT Helpdesk: tables + sample data. Run once in the Supabase SQL Editor.

create table app_user (
    username   text primary key,
    password   text not null,
    full_name  text not null,
    role       text not null check (role in ('REPORTER', 'TECHNICIAN', 'ADMIN'))
);

create table device (
    code       text primary key,
    type       text not null check (type in ('PC', 'PRINTER', 'ROUTER')),
    location   text not null,
    condition  text not null default 'Working'
);

create table ticket (
    id           bigint generated always as identity primary key,
    category     text not null check (category in ('HARDWARE', 'SOFTWARE', 'NETWORK')),
    priority     text not null check (priority in ('LOW', 'MEDIUM', 'HIGH')),
    status       text not null default 'OPEN' check (status in ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED')),
    description  text not null,
    device_code  text not null references device(code),
    reporter     text not null references app_user(username),
    assignee     text references app_user(username),
    created_at   timestamptz not null default now(),
    assigned_at  timestamptz,
    resolved_at  timestamptz
);

create table repair_note (
    id          bigint generated always as identity primary key,
    ticket_id   bigint not null references ticket(id) on delete cascade,
    technician  text not null references app_user(username),
    note        text not null,
    created_at  timestamptz not null default now()
);

-- The desktop app uses the project's public key, so allow it to read and write these tables.
grant select on app_user to anon;
grant select, insert, update on device, ticket, repair_note to anon;
alter table app_user enable row level security;
alter table device enable row level security;
alter table ticket enable row level security;
alter table repair_note enable row level security;
create policy "app access" on app_user for select using (true);
create policy "app access" on device for all using (true) with check (true);
create policy "app access" on ticket for all using (true) with check (true);
create policy "app access" on repair_note for all using (true) with check (true);

insert into app_user values
    ('ana', 'ana123', 'Ana Reyes', 'REPORTER'),
    ('santos', 'santos123', 'Prof. Santos', 'REPORTER'),
    ('carlo', 'tech123', 'Carlo Lim', 'TECHNICIAN'),
    ('jessa', 'tech123', 'Jessa Uy', 'TECHNICIAN'),
    ('admin', 'admin123', 'Admin Cruz', 'ADMIN');

insert into device values
    ('PC-12', 'PC', 'Lab 3', 'Working'),
    ('PC-07', 'PC', 'Lab 3', 'Needs repair'),
    ('PRN-01', 'PRINTER', 'Faculty Room', 'Working'),
    ('RTR-02', 'ROUTER', '2nd Floor', 'Working');

insert into ticket (category, priority, status, description, device_code, reporter, assignee, created_at, assigned_at, resolved_at) values
    ('HARDWARE', 'HIGH', 'OPEN', 'PC won''t turn on', 'PC-07', 'ana', 'carlo', now() - interval '6 hours', now() - interval '5 hours', null),
    ('NETWORK', 'MEDIUM', 'IN_PROGRESS', 'No internet on 2nd floor', 'RTR-02', 'santos', 'jessa', now() - interval '3 hours', now() - interval '2 hours', null),
    ('SOFTWARE', 'LOW', 'OPEN', 'NetBeans missing', 'PC-12', 'ana', null, now() - interval '1 hour', null, null),
    ('HARDWARE', 'MEDIUM', 'RESOLVED', 'Printer paper jam', 'PRN-01', 'santos', 'carlo', now() - interval '2 days', now() - interval '2 days', now() - interval '1 day');

insert into repair_note (ticket_id, technician, note, created_at) values
    (4, 'carlo', 'Removed jammed paper and cleaned rollers.', now() - interval '1 day');
```
</details>
