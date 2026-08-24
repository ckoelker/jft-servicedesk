-- Das Schema des ServiceDesk: drei Tabellen, zwei Beziehungen.
--
-- Firma, Melder, Kategorie und Bearbeiter stehen als Werte am Ticket. Eigene
-- Stammdatentabellen dafuer waeren fachlich richtig, wuerden hier aber nur
-- Joins erzeugen, um die es in dieser Woche nicht geht.
--
-- Die Datenbank ist die Wahrheit, nicht Hibernate: ab Block 12 laeuft die
-- Anwendung mit schema-management.strategy=validate. Ein Feld, das hier anders
-- heisst als in der Entity, faellt beim Start auf.

CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE ticket (
    id           bigserial     PRIMARY KEY,
    kennung      varchar(255)  NOT NULL UNIQUE,
    titel        varchar(255)  NOT NULL,
    beschreibung varchar(2000),
    firma        varchar(255)  NOT NULL,
    melder       varchar(255)  NOT NULL,
    kategorie    varchar(255)  NOT NULL,
    prioritaet   varchar(255)  NOT NULL,
    status       varchar(255)  NOT NULL,
    bearbeiter   varchar(255),
    gemeldet_am  timestamp     NOT NULL,
    erledigt_am  timestamp
);

CREATE TABLE kommentar (
    id             bigserial     PRIMARY KEY,
    text           varchar(2000) NOT NULL,
    autor          varchar(255)  NOT NULL,
    geschrieben_am timestamp     NOT NULL,
    ticket_id      bigint        NOT NULL REFERENCES ticket (id) ON DELETE CASCADE
);

CREATE TABLE zeitbuchung (
    id         bigserial    PRIMARY KEY,
    minuten    integer      NOT NULL,
    bearbeiter varchar(255) NOT NULL,
    ticket_id  bigint       NOT NULL REFERENCES ticket (id) ON DELETE CASCADE
);

CREATE INDEX ix_ticket_status      ON ticket (status);
CREATE INDEX ix_ticket_prioritaet  ON ticket (prioritaet);
CREATE INDEX ix_kommentar_ticket   ON kommentar (ticket_id);
CREATE INDEX ix_zeitbuchung_ticket ON zeitbuchung (ticket_id);
