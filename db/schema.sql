-- SnR IT Helpdesk & Asset Tracking System
-- Database schema for Supabase (PostgreSQL). Run this in the Supabase SQL Editor.

create table app_user (
    id          serial primary key,
    name        text not null,
    role        text not null check (role in ('REPORTER', 'TECHNICIAN', 'ADMIN'))
);

create table device (
    code        text primary key,
    type        text not null check (type in ('PC', 'PRINTER', 'ROUTER')),
    location    text not null,
    condition   text not null default 'Working'
);

create table ticket (
    id           serial primary key,
    category     text not null check (category in ('HARDWARE', 'SOFTWARE', 'NETWORK')),
    priority     text not null check (priority in ('LOW', 'MEDIUM', 'HIGH')),
    status       text not null default 'OPEN'
                 check (status in ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED')),
    description  text not null,
    device_code  text not null references device(code),
    reporter_id  int  not null references app_user(id),
    assignee_id  int  references app_user(id),
    created_at   timestamptz not null default now(),
    assigned_at  timestamptz,
    resolved_at  timestamptz
);

create table repair_note (
    id            serial primary key,
    ticket_id     int  not null references ticket(id) on delete cascade,
    technician_id int  not null references app_user(id),
    note          text not null,
    created_at    timestamptz not null default now()
);
